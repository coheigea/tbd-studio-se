// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.oozie.ui.handler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.talend.core.hadoop.repository.HadoopRepositoryUtil;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.ui.context.model.table.ConectionAdaptContextVariableModel;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.metadata.managment.ui.model.IConnParamName;
import org.talend.metadata.managment.ui.utils.ConnectionContextHelper;
import org.talend.metadata.managment.ui.utils.ExtendedNodeConnectionContextUtils.EHadoopParamName;
import org.talend.metadata.managment.ui.wizard.context.AbstractRepositoryContextHandler;
import org.talend.repository.model.oozie.OozieConnection;

/**
 * created by ldong on Mar 18, 2015 Detailled comment
 *
 */
public class OozieContextHandler extends AbstractRepositoryContextHandler {

    private static final ECodeLanguage LANGUAGE = LanguageManager.getCurrentLanguage();

    @Override
    public boolean isRepositoryConType(Connection connection) {
        return connection instanceof OozieConnection;
    }

    @Override
    public List<IContextParameter> createContextParameters(String prefixName, Connection connection, Set<IConnParamName> paramSet) {
        List<IContextParameter> varList = new ArrayList<IContextParameter>();
        if (connection instanceof OozieConnection) {
            OozieConnection conn = (OozieConnection) connection;

            String paramPrefix = prefixName + ConnectionContextHelper.LINE;
            String paramName = null;
            for (IConnParamName param : paramSet) {
                if (param instanceof EHadoopParamName) {
                    EHadoopParamName hdfsParam = (EHadoopParamName) param;
                    paramName = paramPrefix + hdfsParam;
                    switch (hdfsParam) {
                    case OozieUser:
                        ConnectionContextHelper.createParameters(varList, paramName, conn.getUserName());
                        break;
                    case OozieEndpoint:
                        ConnectionContextHelper.createParameters(varList, paramName, conn.getOozieEndPoind());
                        break;
                    default:
                    }
                }
            }
            createHadoopPropertiesContextVariable(prefixName, varList, conn.getHadoopProperties());
        }
        return varList;
    }

    @Override
    public void setPropertiesForContextMode(String prefixName, Connection connection, Set<IConnParamName> paramSet) {
        if (connection == null) {
            return;
        }
        if (connection instanceof OozieConnection) {
            OozieConnection conn = (OozieConnection) connection;
            String originalVariableName = prefixName + ConnectionContextHelper.LINE;
            String hdfsVariableName = null;
            for (IConnParamName param : paramSet) {
                if (param instanceof EHadoopParamName) {
                    EHadoopParamName hdfsConnectionParam = (EHadoopParamName) param;
                    hdfsVariableName = originalVariableName + hdfsConnectionParam;
                    switch (hdfsConnectionParam) {
                    case OozieUser:
                        conn.setUserName(ContextParameterUtils.getNewScriptCode(hdfsVariableName, LANGUAGE));
                        break;
                    case OozieEndpoint:
                        conn.setOozieEndPoind(ContextParameterUtils.getNewScriptCode(hdfsVariableName, LANGUAGE));
                        break;
                    default:
                    }
                }
            }
            String hadoopProperties = conn.getHadoopProperties();
            List<Map<String, Object>> propertiesAfterContext = transformHadoopPropertiesForContextMode(
                    HadoopRepositoryUtil.getHadoopPropertiesList(hadoopProperties), prefixName);
            conn.setHadoopProperties(HadoopRepositoryUtil.getHadoopPropertiesJsonStr(propertiesAfterContext));
        }
    }

    @Override
    public void setPropertiesForExistContextMode(Connection connection, Set<IConnParamName> paramSet,
            Map<ContextItem, List<ConectionAdaptContextVariableModel>> adaptMap) {
        if (connection == null) {
            return;
        }
        if (connection instanceof OozieConnection) {
            OozieConnection ozzieConn = (OozieConnection) connection;
            ContextItem currentContext = null;
            for (IConnParamName param : paramSet) {
                if (param instanceof EHadoopParamName) {
                    String ozzieVariableName = null;
                    EHadoopParamName ozzieConnectionParam = (EHadoopParamName) param;
                    if (adaptMap != null && adaptMap.size() > 0) {
                        for (Map.Entry<ContextItem, List<ConectionAdaptContextVariableModel>> entry : adaptMap.entrySet()) {
                            currentContext = entry.getKey();
                            List<ConectionAdaptContextVariableModel> modelList = entry.getValue();
                            for (ConectionAdaptContextVariableModel model : modelList) {
                                if (model.getValue().equals(ozzieConnectionParam.name())) {
                                    ozzieVariableName = model.getName();
                                    break;
                                }
                            }
                        }
                    }
                    if (ozzieVariableName != null) {
                        ozzieVariableName = getCorrectVariableName(currentContext, ozzieVariableName, ozzieConnectionParam);
                        matchContextForAttribues(ozzieConn, ozzieConnectionParam, ozzieVariableName);
                    }
                }

            }
            matchAdditionProperties(ozzieConn, adaptMap);
        }
    }

    @Override
    protected void matchContextForAttribues(Connection conn, IConnParamName paramName, String ozzieVariableName) {
        OozieConnection oozieConn = (OozieConnection) conn;
        EHadoopParamName oozieParam = (EHadoopParamName) paramName;
        switch (oozieParam) {
        case OozieUser:
            oozieConn.setUserName(ContextParameterUtils.getNewScriptCode(ozzieVariableName, LANGUAGE));
            break;
        case OozieEndpoint:
            oozieConn.setOozieEndPoind(ContextParameterUtils.getNewScriptCode(ozzieVariableName, LANGUAGE));
            break;
        default:
        }
    }

    @Override
    protected void matchAdditionProperties(Connection conn, Map<ContextItem, List<ConectionAdaptContextVariableModel>> adaptMap) {
        if (conn instanceof OozieConnection) {
            OozieConnection hadoopConn = (OozieConnection) conn;
            if (adaptMap != null && !adaptMap.isEmpty()) {
                List<Map<String, Object>> hadoopListProperties = HadoopRepositoryUtil.getHadoopPropertiesList(hadoopConn
                        .getHadoopProperties());
                Set<String> keys = getConAdditionPropertiesForContextMode(conn);
                for (Map.Entry<ContextItem, List<ConectionAdaptContextVariableModel>> entry : adaptMap.entrySet()) {
                    List<ConectionAdaptContextVariableModel> modelList = entry.getValue();
                    for (ConectionAdaptContextVariableModel model : modelList) {
                        String propertyKey = model.getValue();
                        if (keys.contains(propertyKey)) {
                            List<Map<String, Object>> propertiesAfterContext = transformHadoopPropertiesForExistContextMode(
                                    hadoopListProperties, propertyKey, model.getName());
                            hadoopConn.setHadoopProperties(HadoopRepositoryUtil
                                    .getHadoopPropertiesJsonStr(propertiesAfterContext));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void revertPropertiesForContextMode(Connection connection, ContextType contextType) {
        if (connection instanceof OozieConnection) {
            OozieConnection conn = (OozieConnection) connection;
            String hdfsUser = TalendQuoteUtils.removeQuotes(ContextParameterUtils.getOriginalValue(contextType,
                    conn.getUserName()));
            String endPoint = TalendQuoteUtils.removeQuotes(ContextParameterUtils.getOriginalValue(contextType,
                    conn.getOozieEndPoind()));

            String hadoopProperties = conn.getHadoopProperties();
            List<Map<String, Object>> propertiesAfterRevert = transformContextModeToHadoopProperties(
                    HadoopRepositoryUtil.getHadoopPropertiesList(hadoopProperties), contextType);
            conn.setHadoopProperties(HadoopRepositoryUtil.getHadoopPropertiesJsonStr(propertiesAfterRevert));

            conn.setUserName(hdfsUser);
            conn.setOozieEndPoind(endPoint);
        }
    }

    @Override
    public Set<String> getConAdditionPropertiesForContextMode(Connection conn) {
        Set<String> conVarList = new HashSet<String>();
        if (conn instanceof OozieConnection) {
            OozieConnection oozieConn = (OozieConnection) conn;
            conVarList = getConAdditionProperties(HadoopRepositoryUtil.getHadoopPropertiesList(oozieConn.getHadoopProperties()));
        }
        return conVarList;
    }

}
