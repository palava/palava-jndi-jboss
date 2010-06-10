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

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.jboss.util.naming.NonSerializableFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.cosmocode.palava.jndi.JndiContextBinderUtility;

/** 
 * Jnp {@link JndiContextBinderUtility} implementation.
 * 
 * @author Tobias Sarnowski
 */
public class JnpContextBinderUtility implements JndiContextBinderUtility {
    
    private static final Logger LOG = LoggerFactory.getLogger(JnpContextBinderUtility.class);

    private Provider<Context> context;

    @Inject
    public JnpContextBinderUtility(Provider<Context> context) {
        this.context = context;
    }

    @Override
    public void bind(String jndiName, Object who) throws NamingException {
        LOG.debug("Binding {} to {}", who, jndiName);
        createSubcontext(jndiName);
        context.get().bind(jndiName, who);
    }

    @Override
    public void bind(String jndiName, Object who, Class<?> classType) throws NamingException {
        LOG.debug("Binding {} to {} as static reference with type {}", new Object[] {
            who, jndiName, classType
        });
        createSubcontext(jndiName);

        NonSerializableFactory.bind(jndiName, who);

        // The helper class NonSerializableFactory uses address type nns, we go on to
        // use the helper class to bind the service object in JNDI
        final StringRefAddr addr = new StringRefAddr("nns", jndiName);
        final Reference ref = new Reference(classType.getName(), addr, NonSerializableFactory.class.getName(), null);

        // just register the reference
        context.get().bind(jndiName, ref);
    }

    private void createSubcontext(String jndiName) throws NamingException {
        Context ctx = context.get();
        Name name = ctx.getNameParser("").parse(jndiName);
        while (name.size() > 1) {
            final String ctxName = name.get(0);
            try {
                ctx = (Context) ctx.lookup(ctxName);
                LOG.trace("Subcontext {} already exists", ctxName);
            } catch (NameNotFoundException e) {
                LOG.info("Creating Subcontext {}", ctxName);
                ctx = ctx.createSubcontext(ctxName);
            }
            name = name.getSuffix(1);
        }
    }
    
}
