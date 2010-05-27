/**
 * Copyright 2010 CosmoCode GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.cosmocode.palava.jndi.jnp;

import de.cosmocode.palava.core.lifecycle.Disposable;
import de.cosmocode.palava.core.lifecycle.Initializable;
import de.cosmocode.palava.core.lifecycle.LifecycleException;
import de.cosmocode.palava.jndi.JndiContextProvider;
import org.jnp.interfaces.NamingContext;
import org.jnp.server.Main;
import org.jnp.server.NamingServer;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Installs a local jndi server provided by org.jnp.
 * 
 * @author Tobias Sarnowski
 * @author Willi Schoenborn
 */
final class LocalJndiServer implements Initializable, Disposable, JndiContextProvider {
    
    private final Main main;

    public LocalJndiServer() {
        final NamingServer namingServer;
        
        try {
            namingServer = new NamingServer();
        } catch (NamingException e) {
            throw new LifecycleException(e);
        }
        
        NamingContext.setLocal(namingServer);

        main = new Main();
        main.setInstallGlobalService(true);
        main.setPort(-1);
    }
    
    @Override
    public void initialize() throws LifecycleException {
        try {
            main.start();
        /* CHECKSTYLE:OFF */
        } catch (Exception e) {
        /* CHECKSTYLE:ON */
            throw new LifecycleException(e);
        }
    }

    @Override
    public void dispose() throws LifecycleException {
        main.stop();
    }

	@Override
	public Context get() {
		try {
            //final Properties props = new Properties();
            //props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
            //props.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
            //return new InitialContext(props);
			return new InitialContext();
        } catch (NamingException e) {
            throw new IllegalStateException(e);
        }
	}
}
