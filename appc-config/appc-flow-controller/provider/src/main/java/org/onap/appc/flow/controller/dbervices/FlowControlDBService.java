/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2019 Ericsson
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
 *
 * ============LICENSE_END=========================================================
 */
package org.onap.appc.flow.controller.dbervices;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

import java.util.ArrayList;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.onap.appc.flow.controller.data.Transaction;
import org.onap.appc.flow.controller.utils.EscapeUtils;
import org.onap.appc.flow.controller.utils.FlowControllerConstants;
import org.onap.ccsdk.sli.adaptors.resource.sql.SqlResource;
import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;

public class FlowControlDBService {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(FlowControlDBService.class);
    private static final String QUERY_STR = "Query String : ";
    private static final String FAILURE_PARAM = "FAILURE";
    protected static final String GET_FLOW_REF_DATA_ERROR = "Error - while getting FlowReferenceData ";
    private static final String ARTIFACT_CONTENT_PARAM = "artifact-content";
    protected static final String COUNT_PROTOCOL_PARAM = "count(protocol)";

    private DbLibServiceQueries dblib;
    private static FlowControlDBService dgGeneralDBService = null;

    private FlowControlDBService() {
        if (dblib== null) {
            dblib = new DbLibServiceQueries();
        }
    }

    protected FlowControlDBService(DbLibServiceQueries dbLibServiceQueries) {
            dblib = dbLibServiceQueries;
    }
    
    protected FlowControlDBService(DbLibService dbLibService) {
        dblib = new DbLibServiceQueries(dbLibService);
    }

    public static FlowControlDBService initialise() {
        if (dgGeneralDBService == null) {
            dgGeneralDBService = new FlowControlDBService();
        }
        return dgGeneralDBService;
    }

    public void getFlowReferenceData(SvcLogicContext ctx, Map<String, String> inParams, SvcLogicContext localContext)
        throws SvcLogicException {

        String fn = "DBService.getflowModelInfo";
        String whereClause = " where ACTION = $" + FlowControllerConstants.REQUEST_ACTION;

        if (StringUtils.isNotBlank(ctx.getAttribute(FlowControllerConstants.ACTION_LEVEL))) {
            whereClause = whereClause.concat(" and ACTION_LEVEL = $" + FlowControllerConstants.ACTION_LEVEL);
        }

        QueryStatus status;
        if (dblib != null && localContext != null) {
            String key = "select SEQUENCE_TYPE, CATEGORY, GENERATION_NODE, EXECUTION_NODE from "
                + FlowControllerConstants.DB_MULTISTEP_FLOW_REFERENCE + whereClause;
            log.debug(fn + QUERY_STR + key);
            status = dblib.query(key, localContext);
            if (status.toString().equals(FAILURE_PARAM)) {
                throw new SvcLogicException(GET_FLOW_REF_DATA_ERROR);
            }
        }
    }

    public String getEndPointByAction(String action) {
        return null;
    }

    public String getDesignTimeFlowModel(SvcLogicContext localContext) throws SvcLogicException {
        String fn = "DBService.getDesignTimeFlowModel ";
        QueryStatus status;
        if (dblib != null && localContext != null) {
            String queryString =
                "select max(internal_version) as maxInternalVersion, artifact_name as artifactName from "
                    + FlowControllerConstants.DB_SDC_ARTIFACTS + " where artifact_name in (select artifact_name from "
                    + FlowControllerConstants.DB_SDC_REFERENCE + " where vnf_type= $"
                    + FlowControllerConstants.VNF_TYPE
                    + " and  vnfc_type = $" + FlowControllerConstants.VNFC_TYPE + " and  action = $"
                    + FlowControllerConstants.REQUEST_ACTION + " and file_category =  $"
                    + FlowControllerConstants.CATEGORY + " )";

            log.debug(fn + QUERY_STR + queryString);
            status = dblib.query(queryString, localContext);

            if (status.toString().equals(FAILURE_PARAM)) {
                throw new SvcLogicException(GET_FLOW_REF_DATA_ERROR);
            }

            String queryString1 = "select artifact_content from " + FlowControllerConstants.DB_SDC_ARTIFACTS
                + " where artifact_name = $artifactName  and internal_version = $maxInternalVersion ";

            log.debug(fn + QUERY_STR + queryString1);
            status = dblib.query(queryString1, localContext);
            if (status.toString().equals(FAILURE_PARAM)) {
                throw new SvcLogicException(GET_FLOW_REF_DATA_ERROR);
            }
        }
        return localContext != null ? localContext.getAttribute(ARTIFACT_CONTENT_PARAM) : null;
    }

    public QueryStatus loadSequenceIntoDB(SvcLogicContext localContext) throws SvcLogicException {

        QueryStatus status = null;
        if (localContext != null) {
            String fn = "DBService.saveArtifacts";

            localContext.setAttribute(FlowControllerConstants.ARTIFACT_CONTENT_ESCAPED,
                EscapeUtils.escapeSql(localContext.getAttribute(FlowControllerConstants.ARTIFACT_CONTENT)));
            log.debug("ESCAPED sequence for DB : "
                + localContext.getAttribute(FlowControllerConstants.ARTIFACT_CONTENT_ESCAPED));

            for (Object key : localContext.getAttributeKeySet()) {
                String parmName = (String) key;
                String parmValue = localContext.getAttribute(parmName);
                log.debug(" loadSequenceIntoDB " + parmName + "=" + parmValue);
            }

            String queryString = "INSERT INTO " + FlowControllerConstants.DB_REQUEST_ARTIFACTS + " set request_id =  $"
                + FlowControllerConstants.REQUEST_ID + " , action =  $" + FlowControllerConstants.REQUEST_ACTION
                + " , action_level =  $" + FlowControllerConstants.ACTION_LEVEL + " , vnf_type = $"
                + FlowControllerConstants.VNF_TYPE + " , category = $" + FlowControllerConstants.CATEGORY
                + " , artifact_content = $" + FlowControllerConstants.ARTIFACT_CONTENT_ESCAPED
                + " , updated_date = sysdate() ";

            log.debug(fn + QUERY_STR + queryString);
            status = dblib.save(queryString, localContext);
            if (status.toString().equals(FAILURE_PARAM)) {
                throw new SvcLogicException("Error While processing storing Artifact: "
                    + localContext.getAttribute(FlowControllerConstants.ARTIFACT_NAME));
            }
        }
        return status;

    }

    public void populateModuleAndRPC(Transaction transaction, String vnfType) throws SvcLogicException {
        String fn = "FlowControlDBService.populateModuleAndRPC ";
        QueryStatus status;
        SvcLogicContext context = getSvcLogicContext();
        String protocolType = getProtocolType(transaction, vnfType, fn, context);

        String key = "select execution_type, execution_module, execution_rpc from "
            + FlowControllerConstants.DB_PROCESS_FLOW_REFERENCE + " where action = ? "
            + "and action_level = ? and protocol = ?";
        ArrayList<String> arguments = new ArrayList<>();
        arguments.add(transaction.getAction());
        arguments.add(transaction.getActionLevel());
        arguments.add(protocolType);

        log.debug(fn + QUERY_STR + key);
        status = dblib.query(key, context, arguments);
        if (status.toString().equals(FAILURE_PARAM)) {
            throw new SvcLogicException(GET_FLOW_REF_DATA_ERROR);
        }

        transaction.setExecutionModule(context.getAttribute(FlowControllerConstants.EXECUTTION_MODULE));
        transaction.setExecutionRPC(context.getAttribute(FlowControllerConstants.EXECUTION_RPC));
        transaction.setExecutionType(context.getAttribute(FlowControllerConstants.EXECUTION_TYPE));

    }

    private String getProtocolType(Transaction transaction, String vnfType, String fn, SvcLogicContext context)
        throws SvcLogicException {
        QueryStatus status;
        String protocolQuery;
        int protocolCount;
        protocolQuery = "select count(protocol) from " + FlowControllerConstants.DB_PROTOCOL_REFERENCE
            + " where action = ? and action_level = ?";
        ArrayList<String> arguments = new ArrayList<>();
        arguments.add(transaction.getAction());
        arguments.add(transaction.getActionLevel());

        log.debug(fn + QUERY_STR + protocolQuery);
        status = dblib.query(protocolQuery, context, arguments);
        if (status.toString().equals(FAILURE_PARAM)) {
            throw new SvcLogicException(GET_FLOW_REF_DATA_ERROR);
        }

        String countProtocolParam = context.getAttribute(COUNT_PROTOCOL_PARAM);
        log.debug(" Protocol Count " + countProtocolParam);
        protocolCount = Integer.parseInt(countProtocolParam);

        if (protocolCount == 1) {
            protocolQuery = "select protocol from " + FlowControllerConstants.DB_PROTOCOL_REFERENCE
                + " where action = ? and action_level = ?";
            ArrayList<String> arguments2 = new ArrayList<>();
            arguments2.add(transaction.getAction());
            arguments2.add(transaction.getActionLevel());

            log.debug(fn + QUERY_STR + protocolQuery);
            status = dblib.query(protocolQuery, context, arguments2);
            if (status.toString().equals(FAILURE_PARAM)) {
                throw new SvcLogicException(GET_FLOW_REF_DATA_ERROR);
            }
            return context.getAttribute("protocol");
        } else {
            if (hasSingleProtocol(transaction, vnfType, fn, context)) {
                return context.getAttribute("protocol");
            }
        }
        return null;
    }

    private boolean hasSingleProtocol(Transaction transaction, String vnfType, String fn, SvcLogicContext context)
        throws SvcLogicException {
        String protocolQuery;
        QueryStatus status;
        int protocolCount;
        protocolQuery = "select count(protocol) from " + FlowControllerConstants.DB_PROTOCOL_REFERENCE
            + " where action = ? and action_level = ? and vnf_type = ?";
        ArrayList<String> arguments = new ArrayList<>();
        arguments.add(transaction.getAction());
        arguments.add(transaction.getActionLevel());
        arguments.add(vnfType);

        log.debug(fn + QUERY_STR + protocolQuery);
        status = dblib.query(protocolQuery, context, arguments);
        if (status.toString().equals(FAILURE_PARAM)) {
            throw new SvcLogicException(GET_FLOW_REF_DATA_ERROR);
        }

        log.debug(" Protocol Count " + context.getAttribute(COUNT_PROTOCOL_PARAM));
        protocolCount = Integer.parseInt(context.getAttribute(COUNT_PROTOCOL_PARAM));
        if (protocolCount > 1) {
            throw new SvcLogicException("Got more than 2 values..");
        } else if (protocolCount == 1) {
            protocolQuery = "select protocol from " + FlowControllerConstants.DB_PROTOCOL_REFERENCE
                + " where action = ? and action_level = ? and vnf_type = ?";
            ArrayList<String> arguments2 = new ArrayList<>();
            arguments2.add(transaction.getAction());
            arguments2.add(transaction.getActionLevel());
            arguments2.add(vnfType);
            log.debug(fn + QUERY_STR + protocolQuery);
            status = dblib.query(protocolQuery, context, arguments2);
            if (status.toString().equals(FAILURE_PARAM)) {
                throw new SvcLogicException(GET_FLOW_REF_DATA_ERROR);
            }
            return true;
        }
        return false;
    }

    public String getDependencyInfo(SvcLogicContext localContext) throws SvcLogicException {
        String fn = "DBService.getDependencyInfo ";
        QueryStatus status;
        if (dblib != null && localContext != null) {
            String queryString =
                "select max(internal_version) as maxInternalVersion, artifact_name as artifactName from "
                    + FlowControllerConstants.DB_SDC_ARTIFACTS + " where artifact_name in (select artifact_name from "
                    + FlowControllerConstants.DB_SDC_REFERENCE + " where vnf_type= $"
                    + FlowControllerConstants.VNF_TYPE
                    + " and file_category = '" + FlowControllerConstants.DEPENDENCYMODEL + "' )";

            log.debug(fn + QUERY_STR + queryString);
            status = dblib.query(queryString, localContext);

            if (status.toString().equals(FAILURE_PARAM)) {
                throw new SvcLogicException("Error - while getting dependencydata ");
            }

            String queryString1 = "select artifact_content from " + FlowControllerConstants.DB_SDC_ARTIFACTS
                + " where artifact_name = $artifactName  and internal_version = $maxInternalVersion ";

            log.debug(fn + QUERY_STR + queryString1);
            status = dblib.query(queryString1, localContext);
            if (status.toString().equals(FAILURE_PARAM)) {
                throw new SvcLogicException("Error - while getting dependencyData ");
            }
        }

        return localContext != null ? localContext.getAttribute(ARTIFACT_CONTENT_PARAM) : null;

    }

    public String getCapabilitiesData(SvcLogicContext localContext) throws SvcLogicException {
        String fn = "DBService.getCapabilitiesData ";
        QueryStatus status;
        if (dblib != null && localContext != null) {
            String queryString =
                "select max(internal_version) as maxInternalVersion, artifact_name as artifactName from "
                    + FlowControllerConstants.DB_SDC_ARTIFACTS + " where artifact_name in (select artifact_name from "
                    + FlowControllerConstants.DB_SDC_REFERENCE + " where vnf_type= $"
                    + FlowControllerConstants.VNF_TYPE
                    + " and file_category = '" + FlowControllerConstants.CAPABILITY + "' )";

            log.info(fn + QUERY_STR + queryString);
            status = dblib.query(queryString, localContext);

            if (status.toString().equals(FAILURE_PARAM)) {
                throw new SvcLogicException("Error - while getting capabilitiesData ");
            }

            String queryString1 = "select artifact_content from " + FlowControllerConstants.DB_SDC_ARTIFACTS
                + " where artifact_name = $artifactName  and internal_version = $maxInternalVersion ";

            log.debug(fn + QUERY_STR + queryString1);
            status = dblib.query(queryString1, localContext);
            if (status.toString().equals(FAILURE_PARAM)) {
                throw new SvcLogicException("Error - while getting capabilitiesData ");
            }
        }
        return localContext != null ? localContext.getAttribute(ARTIFACT_CONTENT_PARAM) : null;
    }

    protected SvcLogicContext getSvcLogicContext() {
        return new SvcLogicContext();
    }
}
