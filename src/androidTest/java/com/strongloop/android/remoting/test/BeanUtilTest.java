package com.strongloop.android.remoting.test;

import android.test.MoreAsserts;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.MoreExecutors;
import com.strongloop.android.remoting.BeanUtil;
import com.strongloop.android.remoting.Repository;
import com.strongloop.android.remoting.Transient;
import com.strongloop.android.remoting.VirtualObject;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

public class BeanUtilTest extends TestCase {

    public static class Bean extends VirtualObject {
        private String name;
        private int age;
        private long score;
        private float weight;
        private double answerToAgeOfUniverse;
        private boolean totallyCool;

        // NOTE: Auto-generated getters, setters, and equals
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public long getScore() {
            return score;
        }

        public void setScore(long score) {
            this.score = score;
        }

        public float getWeight() {
            return weight;
        }

        public void setWeight(float weight) {
            this.weight = weight;
        }

        public double getAnswerToAgeOfUniverse() {
            return answerToAgeOfUniverse;
        }

        public void setAnswerToAgeOfUniverse(double answerToAgeOfUniverse) {
            this.answerToAgeOfUniverse = answerToAgeOfUniverse;
        }

        public boolean isTotallyCool() {
            return totallyCool;
        }

        public void setTotallyCool(boolean totallyCool) {
            this.totallyCool = totallyCool;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + age;
            long temp;
            temp = Double.doubleToLongBits(answerToAgeOfUniverse);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + (int) (score ^ (score >>> 32));
            result = prime * result + (totallyCool ? 1231 : 1237);
            result = prime * result + Float.floatToIntBits(weight);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Bean other = (Bean) obj;
            if (age != other.age)
                return false;
            if (Double.doubleToLongBits(answerToAgeOfUniverse) != Double
                    .doubleToLongBits(other.answerToAgeOfUniverse))
                return false;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            if (score != other.score)
                return false;
            if (totallyCool != other.totallyCool)
                return false;
            if (Float.floatToIntBits(weight) != Float
                    .floatToIntBits(other.weight))
                return false;
            return true;
        }

        public Bean() {
            super(new Repository("Bean"), new HashMap<String, Object>());
        }
    }

    public static class Trans extends VirtualObject {
        private String trans;

        @Transient
        public String getTrans() { return trans; }

        @Transient
        public void setTrans(String trans) { this.trans = trans; }
    }

    public void testBean() {
        Bean fromBean = new Bean();
        fromBean.setName("Fred");
        fromBean.setAge(100);
        fromBean.setTotallyCool(true);
        fromBean.setAnswerToAgeOfUniverse(42);
        fromBean.setWeight(9000);
        fromBean.setScore(-100);

        // Convert to properties map and back to the bean, then test for
        // equality. Tests auto-boxing as well.
        //
        // NOTE: Not testing deep copy because it is not used in LoopBack,
        // and there is no analog for setProperties.
        Bean toBean = new Bean();
        Map<String, Object> properties = BeanUtil.getProperties(fromBean,
                true, false);
        BeanUtil.setProperties(toBean, properties, true);
        assertEquals(fromBean, toBean);

        // Test Map with different types (short->int, etc).
        properties = new HashMap<String, Object>();
        properties.put("name", "Fred");
        properties.put("age", (short)100);
        properties.put("totallyCool", true);
        properties.put("answerToAgeOfUniverse", (long)42);
        properties.put("weight", (int)9000);
        properties.put("score", (int)-100);

        Bean bean3 = new Bean();
        BeanUtil.setProperties(bean3, properties, true);
        assertEquals(fromBean, bean3);
    }

    public void testTransient() {
        Trans source = new Trans();
        source.setTrans("transient value");

        Map<String, Object> properties = BeanUtil.getProperties(source, false, true);
        MoreAsserts.assertEquals(
                "getProperties() should have ignored @Transient properties",
                new HashMap<String, Object>().entrySet(), properties.entrySet());

        properties.put("trans", source.getTrans());

        Trans target = new Trans();
        BeanUtil.setProperties(target, properties, true);
        assertNull(
                "setProperties() should have ignored @Transient properties",
                target.getTrans());
    }

    public void testGetPropertiesReturnsOwnPropertiesOnly() {
        Bean bean = new Bean();
        Map<String, Object> ownProperties = BeanUtil.getProperties(bean,
                false, false);

        assertEquals(
                ImmutableSet.of("score", "totallyCool", "weight",
                        "name", "answerToAgeOfUniverse", "age"),
                ownProperties.keySet());
    }

    public void testVirtualObjectHasTransientPropertiesOnly() {
        VirtualObject obj = new VirtualObject();
        MoreAsserts.assertEmpty(BeanUtil.getProperties(obj, true, true));
    }
}
