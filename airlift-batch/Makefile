ROOT := $(abspath .)
BUILD := $(ROOT)/build
JAR := $(ROOT)/mirror/target/airlift-batch-mirror.jar

# Every run of the slice pins the same clock, the same PARM date and the same
# fixture seed, on both sides, so a difference can only come from the code.
export AIRLIFT_CLOCK := 2022071810300012+0000
export AIRLIFT_PARM_DATE := 2022071800

.PHONY: setup inventory inventory-check cobol fixtures golden evidence test \
        java verify verify-reference verify-fixed traps demo clean

## setup: compile the estate's COBOL, generate fixtures, run the real programs
setup: cobol fixtures golden evidence
	@scripts/verify-encoding.sh
	@printf 'airlift-batch setup complete: COBOL golden output in build/golden\n'

## inventory: regenerate INVENTORY.md from app/jcl, app/proc, app/cbl, app/cpy
inventory:
	python3 tools/inventory.py

## inventory-check: fail if INVENTORY.md is not what the tool emits today
inventory-check:
	python3 tools/inventory.py --check

cobol:
	scripts/build-cobol.sh

fixtures:
	mkdir -p $(BUILD)/fixtures
	python3 fixtures/generate.py --out $(BUILD)/fixtures

golden:
	scripts/run-cobol.sh

evidence:
	scripts/evidence.sh

## test: the Java mirror's own unit tests
test:
	mvn -q -f mirror/pom.xml test

$(JAR):
	mvn -q -f mirror/pom.xml -DskipTests package

java: $(JAR)
	scripts/run-java.sh $(JAR) $(BUILD)/java

## verify: field-level parity, COBOL golden output vs the Java mirror
verify: java
	@mkdir -p $(BUILD)/parity
	python3 parity/compare.py $(BUILD)/golden $(BUILD)/java \
		--json $(BUILD)/parity/mismatches.json

## traps: attribute every mismatch to the manifest; unclassified must be 0
traps:
	python3 parity/attribute.py $(BUILD)/parity/mismatches.json

## verify-reference: the untrapped control slice (READACCT/CBACT01C) must be 100%
verify-reference: java
	python3 parity/compare.py $(BUILD)/golden $(BUILD)/java --slice readacct

## verify-fixed: apply parity/fix-traps.patch to a copy and re-verify at 100%
verify-fixed:
	scripts/verify-fixed.sh

## demo: the whole story, trapped verify failing then the fixed mirror at 100%
demo: setup test
	-$(MAKE) verify
	$(MAKE) traps
	$(MAKE) verify-reference
	$(MAKE) verify-fixed

clean:
	rm -rf $(BUILD)
	mvn -q -f mirror/pom.xml clean
