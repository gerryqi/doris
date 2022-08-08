// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.doris.nereids.trees.plans.physical;

import org.apache.doris.nereids.memo.GroupExpression;
import org.apache.doris.nereids.properties.LogicalProperties;
import org.apache.doris.nereids.trees.expressions.Expression;
import org.apache.doris.nereids.trees.expressions.IntegerLiteral;
import org.apache.doris.nereids.trees.plans.Plan;
import org.apache.doris.nereids.trees.plans.PlanType;
import org.apache.doris.nereids.trees.plans.algebra.Limit;
import org.apache.doris.nereids.trees.plans.visitor.PlanVisitor;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Physical limit plan
 */
public class PhysicalLimit<CHILD_TYPE extends Plan> extends PhysicalUnary<CHILD_TYPE> implements Limit {
    private final long limit;

    private final long offset;

    /**
     * constructor
     * select * from t order by a limit [offset], [limit];
     * @param limit the number of tuples retrieved.
     * @param offset the number of tuples skipped.
     */
    public PhysicalLimit(long limit, long offset,
                         Optional<GroupExpression> groupExpression, LogicalProperties logicalProperties,
                         CHILD_TYPE child) {
        super(PlanType.PHYSICAL_LIMIT, groupExpression, logicalProperties, child);
        this.limit = limit;
        this.offset = offset;
    }

    public PhysicalLimit(long limit, long offset,
                         LogicalProperties logicalProperties,
                         CHILD_TYPE child) {
        this(limit, offset, Optional.empty(), logicalProperties, child);
    }

    public long getLimit() {
        return limit;
    }

    public long getOffset() {
        return offset;
    }

    @Override
    public Plan withChildren(List<Plan> children) {
        Preconditions.checkArgument(children.size() == 1);
        return new PhysicalLimit<>(limit, offset, logicalProperties, children.get(0));
    }

    @Override
    public List<Expression> getExpressions() {
        return Lists.newArrayList(
                new IntegerLiteral((int) limit),
                new IntegerLiteral((int) offset)
        );
    }

    @Override
    public Plan withGroupExpression(Optional<GroupExpression> groupExpression) {
        return new PhysicalLimit<>(limit, offset, groupExpression, logicalProperties, child());
    }

    @Override
    public Plan withLogicalProperties(Optional<LogicalProperties> logicalProperties) {
        return new PhysicalLimit<>(limit, offset, logicalProperties.get(), child());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PhysicalLimit that = (PhysicalLimit) o;
        return offset == that.offset && limit == that.limit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset, limit);
    }

    @Override
    public <R, C> R accept(PlanVisitor<R, C> visitor, C context) {
        return visitor.visitPhysicalLimit((PhysicalLimit<Plan>) this, context);
    }

    public String toString() {
        return "PhysicalLimit ( offset=" + offset + ", limit=" + limit + ")";
    }

}
