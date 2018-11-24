import sys


sdk_root = connect_home

execfile(os.path.join(sdk_root, "build", "external_modules.py"))
execfile(os.path.join(sdk_root, "build", "client_modules.py"))
execfile(os.path.join(sdk_root, "build", "internal_modules.py"))
execfile(os.path.join(sdk_root, "build", "plugins_module.py"))

m = module(project_name)

m.depends += ["kzui"]
m.used_libraries += ["connect", "adaptation"]

if "qnx" in platform_name:
    m.env["LIBS"] += ["curl", "socket", "sqlite3", "z", "ssl", "crypto"]
    if "aarch64" in platform_name:
        m.env["CPPDEFINES"] += ["GOOGLE_PROTOBUF_ARCH_AARCH64"]
elif "wayland" in platform_name:
    m.used_libraries += ["curl_ext"]
    m.env["LIBS"] += ["ssl", "crypto"]
elif "android" in platform_name:
    m.used_libraries += ["curl_ext", "openssl", "bearer_udp"]
else:
    m.used_libraries += ["curl_ext", "openssl"]

m.env["CPPPATH"] +=[os.path.join(sdk_root, "ext")]
m.used_libraries += ["v8"]

if GetOption("build-dynamic-libs"):                 # This option is set when --dynamic is used
    m.env["CPPDEFINES"] += ["DECLARE_PLUGIN_ENTRY"] # Tell application NOT to link plugins inside application code
else:
    m.used_libraries +=[ "connect_services" ]

m.output_path = os.path.abspath(os.path.join(project_location, "..", "..", "..", "output", platform_name_with_arch, profile_string))

del m
