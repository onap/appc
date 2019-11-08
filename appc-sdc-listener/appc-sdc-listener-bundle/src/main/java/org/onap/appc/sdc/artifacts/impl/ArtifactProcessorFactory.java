/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.sdc.artifacts.impl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.onap.appc.srvcomm.messaging.event.EventSender;
import org.onap.appc.sdc.artifacts.ArtifactProcessor;
import org.onap.appc.sdc.artifacts.object.ArtifactType;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.notification.INotificationData;
import org.onap.sdc.api.notification.IResourceInstance;

import java.net.URI;

/**
 * Factory class for creating instance of Artifact Processor
 */
public class ArtifactProcessorFactory {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(ArtifactProcessorFactory.class);

    public ArtifactProcessorFactory (){

    }

    /**
     * Provides and instance of Artifact Processor
     * @param client an instance of IDistributionClient
     * @param eventSender an instance of EventSender
     * @param notification an instance of INotificationData
     * @param resource an instance of IResourceInstance
     * @param artifact an instance of IArtifactInfo
     * @param storeUri
     * @return
     */
    public ArtifactProcessor getArtifactProcessor(IDistributionClient client, EventSender eventSender,
                                                         INotificationData notification, IResourceInstance resource,
                                                         IArtifactInfo artifact, URI storeUri) {

        logger.debug("Creating artifact processor for artifact type = " + artifact.getArtifactType());
        ArtifactType artifactType = ArtifactType.getArtifactType(artifact.getArtifactType());
        if(artifactType == null){
            return null;
        }
        ArtifactProcessor artifactProcessor = null;
        switch (artifactType){
            case APPC_CONFIG :
                artifactProcessor = new ConfigArtifactProcessor(client, eventSender, notification, resource,
                        artifact, storeUri);
                break;
            case VF_LICENSE:
                artifactProcessor = new LicenseArtifactProcessor(client,eventSender,notification,resource,
                        artifact,storeUri);
                break;
            default:
                break;
        }
        return artifactProcessor;
    }

}
