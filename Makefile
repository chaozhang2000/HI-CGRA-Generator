# Generate Verilog code
run:
	sbt run

# Run the test
test:
	sbt test

clean:
	rm -rf generated project target test_run_dir	

