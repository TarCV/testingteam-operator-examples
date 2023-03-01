{
  pkgs ? import <nixpkgs> {
    config.android_sdk.accept_license = true;
    config.allowUnfree = true;
  },
  config ? pkgs.config
}:
let
  android = {
    platform = "32";
    buildTools = "30.0.3";
    systemImageType = "google_apis";
    abi = "x86_64";
  };

  sdkArgs = {
    platformVersions = [android.platform];
    buildToolsVersions = [android.buildTools];
    abiVersions = [android.abi];
    systemImageTypes = [android.systemImageType];

    includeSystemImages = true;
    includeEmulator = true;

    # Accepting more licenses declaratively:
    extraLicenses = [
      # Already accepted for you with the global accept_license = true or
      # licenseAccepted = true on androidenv.
      # "android-sdk-license"

      # These aren't, but are useful for more uncommon setups.
      #"android-sdk-preview-license"
      #"android-googletv-license"
      #"android-sdk-arm-dbt-license"
      #"google-gdk-license"
      "intel-android-extra-license"
      "intel-android-sysimage-license"
      #"mips-android-sysimage-license"
    ];
  };

  # TODO: Replace hash with current release when https://github.com/NixOS/nixpkgs/pull/213871 is merged
  androidEnvNixpkgs = builtins.fetchTarball {
    name = "androidenv";
    url = "https://github.com/NixOS/nixpkgs/archive/a05928d7fed88bea451eb865a122197a4aed4e3a.tar.gz";
    # Use nix-prefetch-url --unpack to generate:
    sha256 = "0ghzghwlxhjjj1v68lagkqndmqigy8p6rvf9n76z2bk0pfnqk6rh";
  };
  androidEnv = pkgs.callPackage "${androidEnvNixpkgs}/pkgs/development/mobile/androidenv" {
    inherit config pkgs;
    licenseAccepted = true;
  };

  androidComposition = androidEnv.composeAndroidPackages sdkArgs;
  androidEmulator = androidEnv.emulateApp {
    name = "android-sdk-emulator";
    platformVersion = android.platform;
    abiVersion = android.abi;
    systemImageType = android.systemImageType;
    sdkExtraArgs = sdkArgs;
  };
  androidSdk = androidComposition.androidsdk;
  platformTools = androidComposition.platform-tools;
in
(pkgs.buildFHSUserEnv {
  name = "androidenv";
  targetPkgs = pkgs: ([ androidSdk platformTools pkgs.jdk pkgs.glibc  ]); # androidEmulator

  # If you get libvulkan errors when starting an emulator, try setting the following in <AVD>/config.ini:
  #   hw.gpu.enabled = yes
  #   hw.gpu.mode = software

  profile = ''
    export LANG="C.UTF-8";
    export LC_ALL="C.UTF-8";
    export JAVA_HOME="${pkgs.jdk.home}";
    export ANDROID_SDK_ROOT="${androidSdk}/libexec/android-sdk";

    # Write out local.properties for Android Studio.
    [ -f settings.gradle ] && cat <<EOF > local.properties
    # This file was automatically generated by nix-shell.
    # ${androidEmulator}
    sdk.dir=$ANDROID_SDK_ROOT
    EOF
  '';
}).env
