package org.onap.appc.data.services.node;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import org.onap.appc.data.services.db.DGGeneralDBService;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;

class MockDbServiceBuilder {

    private final DGGeneralDBService dbServiceMock;

    MockDbServiceBuilder() throws SvcLogicException {
        dbServiceMock = mock(DGGeneralDBService.class);
    }

    MockDbServiceBuilder getConfigFileReferenceByFileTypeNVnfType(String prefix, String fileType, SvcLogicResource.QueryStatus status) throws SvcLogicException {
        doReturn(status)
            .when(dbServiceMock)
            .getConfigFileReferenceByFileTypeNVnfType(any(SvcLogicContext.class), eq(prefix), eq(fileType));

        return this;
    }

    public MockDbServiceBuilder getDeviceProtocolByVnfType(String prefix, SvcLogicResource.QueryStatus status) throws SvcLogicException {
        doReturn(status)
            .when(dbServiceMock)
            .getDeviceProtocolByVnfType(any(SvcLogicContext.class), eq(prefix));

        return this;
    }

    public MockDbServiceBuilder getConfigureActionDGByVnfTypeNAction(String prefix, SvcLogicResource.QueryStatus status) throws SvcLogicException {
        doReturn(status)
            .when(dbServiceMock)
            .getConfigureActionDGByVnfTypeNAction(any(SvcLogicContext.class), eq(prefix));

        return this;
    }

    public MockDbServiceBuilder getConfigureActionDGByVnfType(String prefix, SvcLogicResource.QueryStatus status) throws SvcLogicException {
        doReturn(status)
            .when(dbServiceMock)
            .getConfigureActionDGByVnfType(any(SvcLogicContext.class), eq(prefix));

        return this;
    }

    public MockDbServiceBuilder getTemplate(String prefix, String fileCategory, SvcLogicResource.QueryStatus status) throws SvcLogicException {
        doReturn(status)
            .when(dbServiceMock)
            .getTemplate(any(SvcLogicContext.class), eq(prefix), eq(fileCategory));

        return this;
    }

    public MockDbServiceBuilder getTemplateByVnfTypeNAction(String prefix, String fileCategory, SvcLogicResource.QueryStatus status) throws SvcLogicException {
        doReturn(status)
            .when(dbServiceMock)
            .getTemplateByVnfTypeNAction(any(SvcLogicContext.class), eq(prefix), eq(fileCategory));

        return this;
    }

    public MockDbServiceBuilder getTemplateByTemplateName(String prefix, String fileCategory, SvcLogicResource.QueryStatus status) throws SvcLogicException {
        doReturn(status)
            .when(dbServiceMock)
            .getTemplateByTemplateName(any(SvcLogicContext.class), eq(prefix), eq(fileCategory));

        return this;
    }

    public MockDbServiceBuilder saveConfigFiles(String prefix, SvcLogicResource.QueryStatus status) throws SvcLogicException {
        doReturn(status)
            .when(dbServiceMock)
            .saveConfigFiles(any(SvcLogicContext.class), eq(prefix));

        return this;
    }

    public MockDbServiceBuilder getMaxConfigFileId(String prefix, String fileCategory, SvcLogicResource.QueryStatus status) throws SvcLogicException {
        doReturn(status)
            .when(dbServiceMock)
            .getMaxConfigFileId(any(SvcLogicContext.class), eq(prefix), eq(fileCategory));

        return this;
    }

    public MockDbServiceBuilder savePrepareRelationship(String prefix, String field, String sdnc, SvcLogicResource.QueryStatus status) throws SvcLogicException {
        doReturn(status)
            .when(dbServiceMock)
            .savePrepareRelationship(any(SvcLogicContext.class), eq(prefix), eq(field), eq(sdnc));

        return this;
    }

    public MockDbServiceBuilder saveUploadConfig(String prefix, SvcLogicResource.QueryStatus status) throws SvcLogicException {
        doReturn(status)
            .when(dbServiceMock)
            .saveUploadConfig(any(SvcLogicContext.class), eq(prefix));

        return this;
    }

    public MockDbServiceBuilder getUploadConfigInfo(String prefix, SvcLogicResource.QueryStatus status) throws SvcLogicException {
        doReturn(status)
            .when(dbServiceMock)
            .getUploadConfigInfo(any(SvcLogicContext.class), eq(prefix));

        return this;
    }

    public MockDbServiceBuilder updateUploadConfig(String prefix, int maxId, SvcLogicResource.QueryStatus status) throws SvcLogicException {
        doReturn(status)
            .when(dbServiceMock)
            .updateUploadConfig(any(SvcLogicContext.class), eq(prefix), eq(maxId));

        return this;
    }

    public MockDbServiceBuilder getDownloadConfigTemplateByVnf(String prefix, SvcLogicResource.QueryStatus status) throws SvcLogicException {
        doReturn(status)
            .when(dbServiceMock)
            .getDownloadConfigTemplateByVnf(any(SvcLogicContext.class), eq(prefix));

        return this;
    }

    public MockDbServiceBuilder getTemplateByArtifactType(String prefix, String fileCategory, String artifactType, SvcLogicResource.QueryStatus status) throws SvcLogicException {
        doReturn(status)
            .when(dbServiceMock)
            .getTemplateByArtifactType(any(SvcLogicContext.class), eq(prefix), eq(fileCategory), eq(artifactType));

        return this;
    }

    DGGeneralDBService build() {
        return dbServiceMock;
    }
}
