/*
 * Copyright 2012 Red Hat, Inc.
 *
 * This file is part of Thermostat.
 *
 * Thermostat is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your
 * option) any later version.
 *
 * Thermostat is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Thermostat; see the file COPYING.  If not see
 * <http://www.gnu.org/licenses/>.
 *
 * Linking this code with other modules is making a combined work
 * based on this code.  Thus, the terms and conditions of the GNU
 * General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this code give
 * you permission to link this code with independent modules to
 * produce an executable, regardless of the license terms of these
 * independent modules, and to copy and distribute the resulting
 * executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions
 * of the license of that module.  An independent module is a module
 * which is not derived from or based on this code.  If you modify
 * this code, you may extend this exception to your version of the
 * library, but you are not obligated to do so.  If you do not wish
 * to do so, delete this exception statement from your version.
 */

package com.redhat.thermostat.storage.mongodb.internal;

import java.util.Objects;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.redhat.thermostat.storage.core.AbstractQuery;
import com.redhat.thermostat.storage.core.Category;
import com.redhat.thermostat.storage.core.Cursor;
import com.redhat.thermostat.storage.core.Key;
import com.redhat.thermostat.storage.model.Pojo;

public class MongoQuery<T extends Pojo> extends AbstractQuery<T> {

    private MongoStorage storage;
    private BasicDBObject query = new BasicDBObject();
    private boolean hasClauses = false;
    private Category<T> category;
    private Class<T> resultClass;

    MongoQuery(MongoStorage storage, Category<T> category) {
        this.storage = storage;
        this.category = category;
        this.resultClass = category.getDataClass();
    }

    public Category<T> getCategory() {
        return category;
    }

    public void setCategory(Category<T> category) {
        this.category = category;
    }

    @Override
    public <S> void where(Key<S> key, Criteria operator, S value) {
        where(key.getName(), operator, value);
    }

    public void where(String key, Criteria operator, Object value) {
        switch (operator) {
        case EQUALS:
            query.put(key, value);
            break;

        case NOT_EQUAL_TO:
            query.put(key, new BasicDBObject("$ne", value));
            break;

        case LESS_THAN:
            query.put(key, new BasicDBObject("$lt", value));
            break;

        case LESS_THAN_OR_EQUAL_TO:
            query.put(key, new BasicDBObject("$lte", value));
            break;
        case GREATER_THAN:
            query.put(key, new BasicDBObject("$gt", value));
            break;

        case GREATER_THAN_OR_EQUAL_TO:
            query.put(key, new BasicDBObject("$gte", value));
            break;
        default:
            throw new IllegalArgumentException("MongoQuery can not handle " + operator);
        }
        hasClauses = true;
    }

    DBObject getGeneratedQuery() {
        return query;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MongoQuery)) {
            return false;
        }
        MongoQuery<?> other = (MongoQuery<?>) obj;
        return Objects.equals(getCategory(), other.getCategory()) && Objects.equals(this.query, other.query);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCategory(), this.query);
    }

    boolean hasClauses() {
        return hasClauses ;
    }

    @Override
    public Cursor<T> execute() {
        return storage.findAllPojos(this, resultClass);
    }
}
