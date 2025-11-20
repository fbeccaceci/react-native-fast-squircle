require "json"
require_relative './scripts/cocoapods_utils'

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

$config = find_config()
version_flags = "-DREACT_NATIVE_MINOR_VERSION=#{$config[:react_native_minor_version]}"

Pod::Spec.new do |s|
  s.name         = "FastSquircle"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.authors      = package["author"]
  s.swift_version = '5.9'

  s.platforms    = { :ios => min_ios_version_supported }
  s.source       = { :git => "https://github.com/fbeccaceci/react-native-fast-squircle.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,m,mm,cpp,swift}"
  s.private_header_files = "ios/**/*.h"

  s.pod_target_xcconfig = {
    'DEFINES_MODULE' => 'YES',
    'SWIFT_COMPILATION_MODE' => 'wholemodule'
  }

  s.xcconfig = {
    "OTHER_CFLAGS" => "$(inherited) #{version_flags}",
  }

  install_modules_dependencies(s)
end
