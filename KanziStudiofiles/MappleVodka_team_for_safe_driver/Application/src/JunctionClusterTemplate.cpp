#include <kanzi/kanzi.hpp>

#if !defined(KANZI_API_IMPORT) && !defined(DECLARE_PLUGIN_ENTRY)
#define KANZI_CONNECT_PLUGIN_API
#include <connect/kanzi_adaptation/plugins_module.hpp>
#endif

using namespace kanzi;

// Uncomment to compile security certificates into the application binary
// #define USE_COMPILED_CERTIFICATES

#if defined(USE_COMPILED_CERTIFICATES)
#include <connect/security/memorysecuritymaterialstore.hpp>
#include <connect/connection/ssl/clientCertificate.inl>
#endif

class JunctionClusterTemplateApplication : public ExampleApplication
{
public:

    virtual void onConfigure(ApplicationProperties& configuration) KZ_OVERRIDE
    {
        configuration.binaryName = "junctionclustertemplate.kzb.cfg";
    }

    virtual void onProjectLoaded() KZ_OVERRIDE
    {
        // Project file has been loaded from .kzb file.

        // Add initialization code here.


#if defined(USE_COMPILED_CERTIFICATES)
        MemorySecurityMaterialStore::store(MemorySecurityMaterialStore::DefaultClient_ClientCertificate, client_cert_data);
        MemorySecurityMaterialStore::store(MemorySecurityMaterialStore::DefaultClient_ClientPrivateKey, client_pkey_data);
        MemorySecurityMaterialStore::store(MemorySecurityMaterialStore::DefaultClient_ServerCA, server_ca_data);
#endif
    }

protected:
    virtual void registerMetadataOverride(ObjectFactory& factory) KZ_OVERRIDE
    {
        ExampleApplication::registerMetadataOverride(factory);

#if !defined(KANZI_API_IMPORT) && !defined(DECLARE_PLUGIN_ENTRY)
        Domain* domain = getDomain();
        domain->registerModule<PluginsModule>("connect_services");
#endif
    }
};

Application* createApplication()
{
    return new JunctionClusterTemplateApplication;
}
