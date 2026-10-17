/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2026 DBeaver Corp and others
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
package org.jkiss.dbeaver.ext.cubrid.model;

import org.jkiss.dbeaver.model.exec.jdbc.JDBCDatabaseMetaData;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CubridTableExportedKeyTest {

    @Test
    void exportedKeysUseOwnerQualifiedTableName() throws SQLException {
        verifyExportedKeyLookup("owner.orders");
    }

    @Test
    void exportedKeysUseBareTableNameForLegacyDatabases() throws SQLException {
        verifyExportedKeyLookup("orders");
    }

    private static void verifyExportedKeyLookup(String uniqueName) throws SQLException {
        // The real CUBRID JDBC driver uses only the 'table' argument to getExportedKeys;
        // it ignores the separate schema argument even in multi-schema databases.
        CubridTable table = mock(CubridTable.class, Mockito.CALLS_REAL_METHODS);
        doReturn(null).when(table).getCatalog();
        doReturn(null).when(table).getSchema();
        doReturn(uniqueName).when(table).getUniqueName();

        JDBCSession session = mock(JDBCSession.class);
        JDBCDatabaseMetaData metaData = mock(JDBCDatabaseMetaData.class);
        JDBCResultSet resultSet = mock(JDBCResultSet.class);
        when(session.getMetaData()).thenReturn(metaData);
        when(metaData.getExportedKeys(null, null, uniqueName)).thenReturn(resultSet);

        assertTrue(table.loadReferenceInfoList(session, null).isEmpty());

        verify(metaData).getExportedKeys(null, null, uniqueName);
        verify(resultSet).close();
    }
}
