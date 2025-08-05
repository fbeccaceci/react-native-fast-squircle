require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "FastSquircle"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.authors      = package["author"]
  s.swift_version = '5.0'

  s.platforms    = { :ios => min_ios_version_supported }
  s.source       = { :git => "https://github.com/fbeccaceci/react-native-fast-squircle.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,m,mm,cpp,swift}"
  s.private_header_files = "ios/**/*.h"


  install_modules_dependencies(s)
end
