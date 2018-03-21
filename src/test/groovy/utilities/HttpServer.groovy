package utilities

import org.eclipse.jetty.http.HttpVersion
import org.eclipse.jetty.security.ConstraintMapping
import org.eclipse.jetty.security.ConstraintSecurityHandler
import org.eclipse.jetty.security.HashLoginService
import org.eclipse.jetty.security.LoginService
import org.eclipse.jetty.security.authentication.BasicAuthenticator
import org.eclipse.jetty.server.Connector
import org.eclipse.jetty.server.Handler
import org.eclipse.jetty.server.HttpConfiguration
import org.eclipse.jetty.server.HttpConnectionFactory
import org.eclipse.jetty.server.SecureRequestCustomizer
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.server.SslConnectionFactory
import org.eclipse.jetty.server.handler.DefaultHandler
import org.eclipse.jetty.server.handler.HandlerList
import org.eclipse.jetty.server.handler.ResourceHandler
import org.eclipse.jetty.util.security.Constraint
import org.eclipse.jetty.util.ssl.SslContextFactory

import static utilities.ResourcePath.resourcePath

class HttpServer {

    static startServer(base, port=8080) throws Exception {
        println(base)

        Server server = new Server(port as Integer)

        ResourceHandler resource_handler = new ResourceHandler()
        resource_handler.setDirectoriesListed(true)
        resource_handler.setResourceBase(base as String)

        HandlerList handlers = new HandlerList()
        handlers.setHandlers([resource_handler, new DefaultHandler()] as Handler[])
        server.setHandler(handlers)

        server.start()

        return server

    }

    static withServer(base, port=8080, cl) throws Exception {
        println(base)

        Server server = new Server(port as Integer)

        ResourceHandler resource_handler = new ResourceHandler()
        resource_handler.setDirectoriesListed(true)
        resource_handler.setResourceBase(base as String)

        HandlerList handlers = new HandlerList()
        handlers.setHandlers([resource_handler, new DefaultHandler()] as Handler[])
        server.setHandler(handlers)

        server.start()

        cl()

        server.stop()
    }

    static withSecureServerAndBasicAuthentication(base, port=8443, cl) throws Exception {

        String jettyDistKeystore = resourcePath("keystore", "ssl") as String
        String keystorePath = System.getProperty("example.keystore", jettyDistKeystore)
        File keystoreFile = new File(keystorePath)
        if (!keystoreFile.exists()) {
            throw new FileNotFoundException(keystoreFile.getAbsolutePath())
        }

        Server server = new Server()

        HttpConfiguration http_config = new HttpConfiguration()
        http_config.setSecureScheme("https")
        http_config.setSecurePort(port as Integer)
        http_config.setOutputBufferSize(32768)

        SslContextFactory sslContextFactory = new SslContextFactory()
        sslContextFactory.setKeyStorePath(keystoreFile.getAbsolutePath())
        sslContextFactory.setKeyStorePassword("spiderman")
        sslContextFactory.setKeyManagerPassword("spiderman")

        HttpConfiguration https_config = new HttpConfiguration(http_config)
        SecureRequestCustomizer src = new SecureRequestCustomizer()
        https_config.addCustomizer(src)

        ServerConnector https = new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory,HttpVersion.HTTP_1_1.asString()),
                new HttpConnectionFactory(https_config))
        https.setPort(port as Integer)
        https.setIdleTimeout(500000)

        server.setConnectors([https] as Connector[])

        ResourceHandler resource_handler = new ResourceHandler()
        resource_handler.setDirectoriesListed(true)
        resource_handler.setResourceBase(base as String)

        LoginService loginService = new HashLoginService("MyRealm", resourcePath("realm.properties", "ssl") as String)
        server.addBean(loginService)

        ConstraintSecurityHandler security = new ConstraintSecurityHandler()
        server.setHandler(security)

        Constraint constraint = new Constraint()
        constraint.setName("auth")
        constraint.setAuthenticate(true)
        constraint.setRoles(["user", "admin" ] as String[])

        ConstraintMapping mapping = new ConstraintMapping()
        mapping.setPathSpec("/*")
        mapping.setConstraint(constraint)

        security.setConstraintMappings(Collections.singletonList(mapping))
        security.setAuthenticator(new BasicAuthenticator())
        security.setLoginService(loginService)

        security.setHandler(resource_handler)

        server.start()

        cl()

        server.stop()
    }
}
