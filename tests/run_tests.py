#!/usr/bin/env python3

import unittest
import sys
import time
import logging
from pathlib import Path

def setup_logging():
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
        handlers=[
            logging.StreamHandler(sys.stdout),
            logging.FileHandler('test_results.log')
        ]
    )

def discover_and_run_tests():
    setup_logging()
    logger = logging.getLogger(__name__)
    
    logger.info("Starting CICS CC00 Login Transaction Test Suite")
    logger.info("=" * 60)
    
    start_time = time.time()
    
    test_dir = Path(__file__).parent
    loader = unittest.TestLoader()
    
    test_suites = []
    
    test_modules = [
        'scenarios.valid_login_tests',
        'scenarios.invalid_login_tests', 
        'scenarios.edge_case_tests'
    ]
    
    for module_name in test_modules:
        try:
            suite = loader.loadTestsFromName(f'tests.{module_name}')
            test_suites.append(suite)
            logger.info(f"Loaded test module: {module_name}")
        except Exception as e:
            logger.error(f"Failed to load test module {module_name}: {e}")
    
    combined_suite = unittest.TestSuite(test_suites)
    
    runner = unittest.TextTestRunner(
        verbosity=2,
        stream=sys.stdout,
        buffer=True
    )
    
    logger.info("Executing test suite...")
    logger.info("-" * 60)
    
    result = runner.run(combined_suite)
    
    elapsed_time = time.time() - start_time
    
    logger.info("=" * 60)
    logger.info("Test Suite Execution Summary")
    logger.info("=" * 60)
    logger.info(f"Total execution time: {elapsed_time:.2f} seconds")
    logger.info(f"Tests run: {result.testsRun}")
    logger.info(f"Failures: {len(result.failures)}")
    logger.info(f"Errors: {len(result.errors)}")
    logger.info(f"Skipped: {len(result.skipped) if hasattr(result, 'skipped') else 0}")
    
    if result.failures:
        logger.error("FAILURES:")
        for test, traceback in result.failures:
            logger.error(f"  {test}: {traceback}")
    
    if result.errors:
        logger.error("ERRORS:")
        for test, traceback in result.errors:
            logger.error(f"  {test}: {traceback}")
    
    success_rate = ((result.testsRun - len(result.failures) - len(result.errors)) / result.testsRun * 100) if result.testsRun > 0 else 0
    logger.info(f"Success rate: {success_rate:.1f}%")
    
    if result.wasSuccessful():
        logger.info("✅ All tests passed successfully!")
        return 0
    else:
        logger.error("❌ Some tests failed!")
        return 1

if __name__ == '__main__':
    sys.exit(discover_and_run_tests())
