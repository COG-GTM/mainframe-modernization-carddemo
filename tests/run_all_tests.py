#!/usr/bin/env python3

import unittest
import sys
import os
import logging
import time
from datetime import datetime

sys.path.insert(0, os.path.dirname(__file__))

def setup_logging():
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
        handlers=[
            logging.StreamHandler(sys.stdout),
            logging.FileHandler('test_results.log')
        ]
    )

def run_test_suite():
    setup_logging()
    logger = logging.getLogger(__name__)
    
    logger.info("=" * 80)
    logger.info("CICS CC00 Login Transaction Test Suite")
    logger.info("Repository: COG-GTM/mainframe-modernization-carddemo")
    logger.info("Jira Ticket: MBA-215")
    logger.info("=" * 80)
    
    start_time = time.time()
    
    loader = unittest.TestLoader()
    suite = loader.discover('.', pattern='*_tests.py')
    
    runner = unittest.TextTestRunner(
        verbosity=2,
        stream=sys.stdout,
        buffer=True
    )
    
    logger.info(f"Starting test execution at {datetime.now()}")
    result = runner.run(suite)
    
    end_time = time.time()
    execution_time = end_time - start_time
    
    logger.info("=" * 80)
    logger.info("TEST EXECUTION SUMMARY")
    logger.info("=" * 80)
    logger.info(f"Total tests run: {result.testsRun}")
    logger.info(f"Failures: {len(result.failures)}")
    logger.info(f"Errors: {len(result.errors)}")
    logger.info(f"Skipped: {len(result.skipped) if hasattr(result, 'skipped') else 0}")
    logger.info(f"Execution time: {execution_time:.3f} seconds")
    logger.info(f"Average time per test: {execution_time/result.testsRun:.3f} seconds")
    
    if result.failures:
        logger.error("FAILURES:")
        for test, traceback in result.failures:
            logger.error(f"  - {test}: {traceback}")
    
    if result.errors:
        logger.error("ERRORS:")
        for test, traceback in result.errors:
            logger.error(f"  - {test}: {traceback}")
    
    success = len(result.failures) == 0 and len(result.errors) == 0
    
    if success:
        logger.info("✅ ALL TESTS PASSED - Test suite execution successful!")
        logger.info("✅ CICS CC00 login transaction testing framework is ready for production use")
    else:
        logger.error("❌ TEST SUITE FAILED - Please review failures and errors above")
        return 1
    
    logger.info("=" * 80)
    return 0

if __name__ == '__main__':
    sys.exit(run_test_suite())
