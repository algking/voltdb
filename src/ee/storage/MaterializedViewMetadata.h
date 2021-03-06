/* This file is part of VoltDB.
 * Copyright (C) 2008-2013 VoltDB Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with VoltDB.  If not, see <http://www.gnu.org/licenses/>.
 */

#ifndef MATERIALIZEDVIEWMETADATA_H_
#define MATERIALIZEDVIEWMETADATA_H_

#include <vector>

#include "common/types.h"
#include "common/tabletuple.h"
#include "catalog/materializedviewinfo.h"

namespace voltdb {

class AbstractExpression;
class PersistentTable;
class TableIndex;

/**
 * Manage the inserts, deletes and updates for a materialized view table based on changes to
 * a source table. An instance sits between the two tables translasting changes in one table
 * into changes in another table. It loads all this information from the catalog in its
 * constructor.
 */
class MaterializedViewMetadata {
public:

    MaterializedViewMetadata(PersistentTable *srcTable, PersistentTable *destTable, catalog::MaterializedViewInfo *mvInfo);
    ~MaterializedViewMetadata();

    /**
     * Called when the source table is inserting a tuple. This will update the materialized view
     * destination table to reflect this change.
     */
    void processTupleInsert(TableTuple &newTuple, bool fallible);

    /**
     * Called when the source table is deleting a tuple. This will update the materialized view
     * destination table to reflect this change.
     */
    void processTupleDelete(TableTuple &oldTuple, bool fallible);

    PersistentTable * targetTable() const { return m_target; }

    void setTargetTable(PersistentTable * target);
private:

    void freeBackedTuples();
    void allocateBackedTuples();

    /** load a predicate from the catalog structure if it's there */
    void parsePredicate(catalog::MaterializedViewInfo *mvInfo);

    void parseComplexGroupby(catalog::MaterializedViewInfo *mvInfo);

    /**
     * build a search key based on the src table value
     * and use an index to find 0 or 1 rows in the view table
     */
    bool findExistingTuple(TableTuple &oldTuple, bool expected = false);

    // the materialized view table
    PersistentTable *m_target;
    // space to hold the search key for the view table
    TableTuple m_searchKey;
    // storage to hold the value for the search key
    char *m_searchKeyBackingStore;
    // the primary index on the view table whose columns
    // are the same as the group by in the view query
    TableIndex *m_index;

    // space to store temp view tuples
    TableTuple m_existingTuple;
    TableTuple m_updatedTuple;
    char *m_updatedTupleBackingStore;
    TableTuple m_emptyTuple;
    char *m_emptyTupleBackingStore;

    // predicate to include or exclude rows from being
    // part of the aggregation in the materialized view
    AbstractExpression *m_filterPredicate;

    bool m_hasComplexGroupby;
    std::vector<AbstractExpression *> m_groupbyExprs;

    // how many columns is the view aggregated on
    int32_t m_groupByColumnCount;
    // which columns in the source table
    int32_t *m_groupByColumns;

    // how many columns in the materialized view table
    int32_t m_outputColumnCount;
    // what are the indexes of columns in the src table for
    // the columns in the view table
    int32_t *m_outputColumnSrcTableIndexes;
    // what are the aggregates for each column in the view table
    ExpressionType *m_outputColumnAggTypes;

    // vector of target table indexes to update.
    // Ideally, these should be a subset of the target table indexes that depend on the count and/or
    // aggregated columns, but there might be some other mostly harmless ones in there that are based
    // solely on the immutable primary key (GROUP BY columns).
    std::vector<TableIndex*> m_updatableIndexList;
};

} // namespace voltdb

#endif // MATERIALIZEDVIEWMETADATA_H_
