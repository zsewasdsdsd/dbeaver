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
package org.jkiss.dbeaver.model.sql;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.jkiss.dbeaver.model.impl.sql.BasicSQLDialect;
import org.jkiss.dbeaver.model.sql.parser.SQLSemanticProcessor;
import org.jkiss.dbeaver.model.sql.transformers.SQLQueryTransformerCount;
import org.jkiss.junit.DBeaverUnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SQLQueryTransformerCountTest extends DBeaverUnitTest {

    @Test
    public void acceptsCommonTableExpressionSelect() throws Exception {
        Statement statement = SQLSemanticProcessor.parseQuery(
            BasicSQLDialect.INSTANCE,
            "WITH active_users AS (SELECT id FROM users WHERE active = 1) SELECT id FROM active_users"
        );

        Assertions.assertTrue(SQLQueryTransformerCount.isCountableSelect(statement));
    }

    @Test
    public void acceptsNonPlainSelectSyntaxTrees() {
        ParenthesedSelect statement = new ParenthesedSelect();

        Assertions.assertFalse(PlainSelect.class.isInstance(statement));
        Assertions.assertTrue(SQLQueryTransformerCount.isCountableSelect(statement));
    }

    @Test
    public void acceptsUnionSelect() throws Exception {
        Statement statement = SQLSemanticProcessor.parseQuery(
            BasicSQLDialect.INSTANCE,
            "SELECT id FROM users UNION ALL SELECT id FROM archived_users"
        );

        Assertions.assertTrue(SQLQueryTransformerCount.isCountableSelect(statement));
    }

    @Test
    public void rejectsNonSelectStatements() throws Exception {
        Statement update = SQLSemanticProcessor.parseQuery(
            BasicSQLDialect.INSTANCE,
            "UPDATE users SET active = 0 WHERE id = 1"
        );
        Statement delete = SQLSemanticProcessor.parseQuery(
            BasicSQLDialect.INSTANCE,
            "DELETE FROM users WHERE id = 1"
        );

        Assertions.assertFalse(SQLQueryTransformerCount.isCountableSelect(update));
        Assertions.assertFalse(SQLQueryTransformerCount.isCountableSelect(delete));
        Assertions.assertFalse(SQLQueryTransformerCount.isCountableSelect(null));
    }
}
