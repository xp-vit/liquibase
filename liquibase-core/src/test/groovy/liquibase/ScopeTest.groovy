package liquibase

import liquibase.actionlogic.ActionLogicFactory
import spock.lang.Specification

class ScopeTest extends Specification {

    def "root scope creation and values"() {
        expect:
        def scope = new Scope(null)
        scope.getParent() == null
        scope.get("x", String) == null
        scope.get("y", String) == null

        def child = scope.child("x", 12)
        child.get("x", String) == "12"
        child.get("y", String) == null
    }

    def "nested scope logic"() {
        when:
        def rootScope = new Scope(["root-1"  : 1,
                                   "root-2"  : 2,
                                   "override": "root"])
        def childScope = rootScope.child(["child-1" : "c1",
                                          "child-2" : "c2",
                                          "override": "child"])
        def grandScope1 = childScope.child(["g1-1"    : "g1.1",
                                            "g1-2"    : "g1.2",
                                            "override": "g1"])
        def grandScope2 = childScope.child(["g2-1"    : "g2.1",
                                            "g2-2"    : "g2.2",
                                            "override": "g2"])

        then: "standard get method works"
        rootScope.get("root-1", String) == "1"
        rootScope.get("root-2", String) == "2"
        rootScope.get("child-1", String) == null
        rootScope.get("g2-2", String) == null
        rootScope.get("override", String) == "root"

        childScope.get("root-1", String) == "1"
        childScope.get("root-2", String) == "2"
        childScope.get("child-1", String) == "c1"
        childScope.get("child-2", String) == "c2"
        childScope.get("g2-2", String) == null
        childScope.get("override", String) == "child"

        grandScope1.get("root-1", String) == "1"
        grandScope1.get("child-2", String) == "c2"
        grandScope1.get("g1-1", String) == "g1.1"
        grandScope1.get("g1-2", String) == "g1.2"
        grandScope1.get("g2-2", String) == null
        grandScope1.get("override", String) == "g1"

        grandScope2.get("root-1", String) == "1"
        grandScope2.get("child-2", String) == "c2"
        grandScope2.get("g1-1", String) == null
        grandScope2.get("g1-2", String) == null
        grandScope2.get("g2-1", String) == "g2.1"
        grandScope2.get("g2-2", String) == "g2.2"
        grandScope2.get("override", String) == "g2"

        and: "default value method works"
        rootScope.get("root-1", "def") == "1"
        rootScope.get("root-2", "def") == "2"
        rootScope.get("root-3", "def") == "def"

        childScope.get("root-2", "def") == "2"
        childScope.get("root-3", "def") == "def"
        childScope.get("child-1", "def") == "c1"
        childScope.get("child-X", "def") == "def"

        grandScope1.get("root-1", "def") == "1"
        grandScope1.get("child-2", "def") == "c2"
        grandScope1.get("g1-1", "def") == "g1.1"
        grandScope1.get("override", "def") == "g1"
        grandScope1.get("invalid", "def") == "def"
    }

    def "getSingleton"() {
        when:
        def rootScope = new Scope(new HashMap<String, Object>())
        def childScope = rootScope.child(new HashMap<String, Object>())
        def grandScope1 = childScope.child(new HashMap<String, Object>())
        def grandScope2 = childScope.child(new HashMap<String, Object>())

        def actionLogicFactory = grandScope2.getSingleton(ActionLogicFactory)

        then:
        grandScope1.getSingleton(ActionLogicFactory).is(actionLogicFactory)
        childScope.getSingleton(ActionLogicFactory).is(actionLogicFactory)
        rootScope.getSingleton(ActionLogicFactory).is(actionLogicFactory)
    }

}
