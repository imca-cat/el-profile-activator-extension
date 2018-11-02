/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Modifications copyright 2018 J. Lewis Muir
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.imca_cat.maven.profile_activation_advanced;

import org.apache.maven.model.Activation;
import org.apache.maven.model.ActivationProperty;
import org.apache.maven.model.Profile;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.profile.ProfileActivationContext;
import org.apache.maven.model.profile.activation.ProfileActivator;
import org.apache.maven.model.profile.activation.PropertyProfileActivator;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.mvel2.CompileException;
import org.mvel2.MVEL;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A property-based {@link ProfileActivator} that evaluates an
 * <a href="https://github.com/mvel/mvel">MVEL</a>
 * expression on system and user properties to determine activation.
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
@Component(role = ProfileActivator.class, hint = "property")
public class AdvancedProfileActivator implements ProfileActivator {
    private static final Pattern COMMA_PAT = Pattern.compile(",");
    private static final Pattern MVEL_SCRIPT_PROPERTY_NAME_PAT = Pattern.compile("^mvel(?:\\(([^\\)]*+)\\))?+$");

    @Requirement
    private Logger logger;

    /**
     * Constructs an instance.
     */
    public AdvancedProfileActivator() {
      super();
    }

    /**
     * Determines whether the specified profile is active using the specified
     * activation context and problem collector.
     * <p>
     * If the activation property name equals {@code "mvel"}, or equals
     * {@code "mvel("} followed by a properties-map identifier followed by
     * {@code ")"}, the MVEL evaluation mode is triggered, and the activation
     * property value is evaluated as an MVEL expression to determine whether
     * the profile is active.  If the MVEL evaluation mode is not triggered,
     * or if the MVEL expression evaluates to {@code false} or is invalid, a
     * {@link PropertyProfileActivator} instance is used to determine whether
     * the profile is active (i.e., the activation behaves like a normal
     * property activation).
     *
     * @param profile the profile whose activation status should be
     *          determined; must not be {@code null}
     * @param context the environmental context used to determine the
     *          activation status of the profile; must not be {@code null}
     * @param problems the container used to collect problems (e.g., bad
     *          syntax) that were encountered; must not be {@code null}
     *
     * @return {@code true} if the profile is active; {@code false} otherwise
     */
    @Override
    public boolean isActive(Profile profile, ProfileActivationContext context, ModelProblemCollector problems) {
        Activation activation = profile.getActivation();

        boolean result = false;

        if (activation != null)
        {
            ActivationProperty property = activation.getProperty();

            if (property != null)
            {
                String name = property.getName();

                Matcher matcher = MVEL_SCRIPT_PROPERTY_NAME_PAT.matcher((name == null) ? "" : name);
                if (matcher.matches()) {
                    List<String> parameters = Collections.<String>emptyList();
                    String parametersString = matcher.group(1);
                    parametersString = (parametersString == null) ? "" : parametersString.trim();
                    if (!parametersString.isEmpty()) {
                        parameters = Arrays.asList(COMMA_PAT.split(parametersString, -1));
                    }
                    String value = property.getValue();
                    logger.debug("Evaluating following MVEL expression: " + value);
                    result = evaluateMvel(value, parameters, context, problems);
                    logger.debug("Evaluated MVEL expression: " + value + " as " + result);
                }
            }
        }

        // call original implementation if mvel script was not valid/false
        return result ? true : new PropertyProfileActivator().isActive(profile, context, problems);
    }

    /**
     * Determines whether an activation modeled by this activator is present
     * in the specified profile using the specified context and problem
     * collector.
     *
     * @param profile the profile to inspect for the presence of an activation
     *          modeled by this activator; must not be {@code null}
     * @param context the environmental context used to determine the presence
     *          of the activation in the profile; must not be {@code null}
     * @param problems the container used to collect problems (e.g., bad
     *          syntax) that were encountered; must not be {@code null}
     *
     * @return {@code true} if the activation is present; {@code false}
     *           otherwise
     */
    @Override
    public boolean presentInConfig(Profile profile, ProfileActivationContext context, ModelProblemCollector problems) {
        return new PropertyProfileActivator().presentInConfig(profile, context, problems);
    }

    private boolean evaluateMvel(String expression, List<String> parameters, ProfileActivationContext context, ModelProblemCollector problems) {
        if (expression == null || expression.length() == 0) {
            return false;
        }

        String propertiesIdentifier = null;
        Iterator<String> parametersIterator = parameters.iterator();
        if (parametersIterator.hasNext()) {
            String identifier = parametersIterator.next();
            identifier = (identifier == null) ? "" : identifier.trim();
            if (!identifier.isEmpty()) {
                propertiesIdentifier = identifier;
            }
        }

        try {
            // "casting" to <String,Object> and including both user and system properties
            Map<String, Object> properties = new HashMap<String, Object>();
            properties.putAll(context.getSystemProperties());
            properties.putAll(context.getUserProperties());
            Map<String, Object> externalVariables = properties;
            if (propertiesIdentifier != null) {
                externalVariables = new HashMap<String, Object>();
                externalVariables.putAll(properties);
                // including properties map as specified identifier
                externalVariables.put(propertiesIdentifier, properties);
            }

            return MVEL.evalToBoolean(expression, externalVariables);
        } catch (NullPointerException e) {
            logger.warn("Unable to evaluate mvel property value (\"" + expression + "\")");
            logger.debug(e.getMessage());
            return false;
        } catch (CompileException e) {
            logger.warn("Unable to evaluate mvel property value (\"" + expression + "\")");
            logger.debug(e.getMessage());
            return false;
        }
    }
}
