/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.enforcer.rules.version;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rules.AbstractStandardEnforcerRule;

import static org.apache.maven.enforcer.rules.utils.ArtifactMatcher.containsVersion;

/**
 * Contains the common code to compare a version against a version range.
 *
 * @author <a href="mailto:brianf@apache.org">Brian Fox</a>
 */
abstract class AbstractVersionEnforcer extends AbstractStandardEnforcerRule {

    /**
     * Specify the required version. Some examples are:
     * <ul>
     * <li><code>2.0.4</code> Version 2.0.4 and higher (different from Maven meaning)</li>
     * <li><code>[2.0,2.1)</code> Versions 2.0 (included) to 2.1 (not included)</li>
     * <li><code>[2.0,2.1]</code> Versions 2.0 to 2.1 (both included)</li>
     * <li><code>[2.0.5,)</code> Versions 2.0.5 and higher</li>
     * <li><code>(,2.0.5],[2.1.1,)</code> Versions up to 2.0.5 (included) and 2.1.1 or higher</li>
     * </ul>
     *
     * @see {@link #setVersion(String)}
     * @see {@link #getVersion()}
     */
    private String version;

    /**
     * Compares the specified version to see if it is allowed by the defined version range.
     *
     * @param variableName         name of variable to use in messages. For example, "Maven" or "Java".
     * @param requiredVersionRange range of allowed versions
     * @param actualVersion        the version to be checked
     * @throws EnforcerRuleException the enforcer rule exception
     */
    // CHECKSTYLE_OFF: LineLength
    public void enforceVersion(String variableName, String requiredVersionRange, ArtifactVersion actualVersion)
            throws EnforcerRuleException
                // CHECKSTYLE_ON: LineLength
            {
        if (requiredVersionRange == null || requiredVersionRange.isEmpty()) {
            throw new EnforcerRuleException(variableName + " version can't be empty.");
        } else {

            String msg = "Detected " + variableName + " Version: " + actualVersion;

            // short circuit check if the strings are exactly equal
            if (actualVersion.toString().equals(requiredVersionRange)) {
                getLog().debug(msg + " is allowed in the range " + requiredVersionRange + ".");
            } else {
                try {
                    VersionRange versionRange = VersionRange.createFromVersionSpec(requiredVersionRange);

                    if (containsVersion(versionRange, actualVersion)) {
                        getLog().debug(msg + " is allowed in the range " + toString(versionRange) + ".");
                    } else {
                        String message = getMessage();

                        if (message == null || message.isEmpty()) {
                            message = msg + " is not in the allowed range " + toString(versionRange) + ".";
                        }

                        throw new EnforcerRuleException(message);
                    }
                } catch (InvalidVersionSpecificationException e) {
                    throw new EnforcerRuleException(
                            "The requested " + variableName + " version " + requiredVersionRange + " is invalid.", e);
                }
            }
        }
    }

    protected static String toString(VersionRange versionRange) {
        // as recommended version is used as lower bound in this context modify the string representation
        if (versionRange.getRecommendedVersion() != null) {
            return "[" + versionRange.getRecommendedVersion().toString() + ",)";
        } else {
            return versionRange.toString();
        }
    }

    @Override
    public String getCacheId() {
        if (version != null && !version.isEmpty()) {
            // return the hash codes of the parameter that matters
            return "" + version.hashCode();
        } else {
            return "0";
        }
    }

    /**
     * Gets the required version.
     *
     * @return the required version
     */
    public final String getVersion() {
        return this.version;
    }

    /**
     * Specify the required version. Some examples are:
     * <ul>
     * <li><code>2.0.4</code> Version 2.0.4 and higher (different from Maven meaning)</li>
     * <li><code>[2.0,2.1)</code> Versions 2.0 (included) to 2.1 (not included)</li>
     * <li><code>[2.0,2.1]</code> Versions 2.0 to 2.1 (both included)</li>
     * <li><code>[2.0.5,)</code> Versions 2.0.5 and higher</li>
     * <li><code>(,2.0.5],[2.1.1,)</code> Versions up to 2.0.5 (included) and 2.1.1 or higher</li>
     * </ul>
     *
     * @param version the required version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }
}
