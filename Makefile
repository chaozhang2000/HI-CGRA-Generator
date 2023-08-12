TOP = TopMain
SIM_TOP = simMain
BUILD_DIR = ./build
OUT_DIR = ./out
TOP_V = $(BUILD_DIR)/$(TOP).v
SCALA_FILE = $(shell find ./src/main/scala -name '*.scala')
SCALA_TEST_FILE = $(shell find ./src/main/scala -name '*.scala')


.DEFAULT_GOAL = verilog

help:
	./mill chiselModule.runMain top.$(TOP) --help 

$(TOP_V): $(SCALA_FILE)
	mkdir -p $(@D)
	./mill chiselModule.runMain top.$(TOP) -td $(@D) --output-file $(@F) 

verilog: $(TOP_V)

chiselsim: $(SCALA_FILE) $(SCALA_TEST_FILE)
	./mill chiselModule.runMain sim.$(SIM_TOP)
clean:
	rm -rf $(BUILD_DIR)
cleanall:
	rm -rf $(BUILD_DIR) $(OUT_DIR)

.PHONY: verilog clean cleanall help 
