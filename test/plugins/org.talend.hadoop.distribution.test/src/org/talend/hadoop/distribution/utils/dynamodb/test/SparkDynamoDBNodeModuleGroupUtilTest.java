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
package org.talend.hadoop.distribution.utils.dynamodb.test;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;
import org.talend.hadoop.distribution.DistributionModuleGroup;
import org.talend.hadoop.distribution.condition.common.SparkBatchLinkedNodeCondition;
import org.talend.hadoop.distribution.utils.dynamodb.SparkDynamoDBNodeModuleGroupUtil;

@SuppressWarnings("nls")
public class SparkDynamoDBNodeModuleGroupUtilTest {

    String groupName = "GROUPNAME";

    String distribution = "dist1";

    String version = "v1";

    @Test
    public void getModuleGroupsTest_withoutAdditionCondition() {
        Set<DistributionModuleGroup> groups = SparkDynamoDBNodeModuleGroupUtil.getModuleGroups(distribution, version, null,
                groupName);
        assertEquals(groups.size(), 1);
        DistributionModuleGroup dmg = groups.iterator().next();
        SparkBatchLinkedNodeCondition underlyingCondition = new SparkBatchLinkedNodeCondition(distribution, version);
        assertEquals(dmg.getModuleName(), groupName);
        // Without an additional condition, the returned module group is actually a SparkBatchLinkedNodeCondition
        assertEquals(dmg.getRequiredIf().getConditionString(), underlyingCondition.getCondition().getConditionString());
    }

    @Test
    public void getModuleGroupsTest_withAdditionCondition() {
        String conditon = "A == 'true'";
        Set<DistributionModuleGroup> groups = SparkDynamoDBNodeModuleGroupUtil.getModuleGroups(distribution, version, conditon,
                groupName);
        assertEquals(groups.size(), 1);
        DistributionModuleGroup dmg = groups.iterator().next();
        SparkBatchLinkedNodeCondition underlyingCondition = new SparkBatchLinkedNodeCondition(distribution, version);
        assertEquals(dmg.getModuleName(), groupName);
        assertEquals(dmg.getRequiredIf().getConditionString(), "(" + conditon + ") AND "
                + underlyingCondition.getCondition().getConditionString());

    }

}