import unittest
import logging
import time
import sys
import os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))
from framework.cics_simulator import CICSSimulator, AIDKey
from validation.commarea_validators import COMMAreaValidator
from validation.screen_validators import ScreenValidator

class ValidLoginTests(unittest.TestCase):
    def setUp(self):
        self.simulator = CICSSimulator()
        self.commarea_validator = COMMAreaValidator()
        self.screen_validator = ScreenValidator()
        self.start_time = time.time()
        
        logging.basicConfig(level=logging.INFO)
        self.logger = logging.getLogger(__name__)
    
    def tearDown(self):
        elapsed_time = time.time() - self.start_time
        self.logger.info(f"Test completed in {elapsed_time:.2f} seconds")
        self.assertLess(elapsed_time, 30, "Test should complete within 30 seconds")
    
    def test_admin_user_valid_login(self):
        user_id = 'ADMIN001'
        password = 'PASSWORD'
        
        self.logger.info(f"Testing admin user login: {user_id}")
        
        result = self.simulator.simulate_login_transaction(user_id, password)
        
        self.assertTrue(result['success'], f"Login should succeed for {user_id}")
        self.assertEqual(result['target_program'], 'COADM01C', 
                        "Admin user should transfer to COADM01C")
        self.assertEqual(result['error_message'], '', 
                        "Successful login should have no error message")
        
        commarea = result['commarea']
        validation_result = self.commarea_validator.validate_admin_user_flow(commarea, user_id)
        
        self.assertTrue(validation_result['valid'], 
                       f"COMMAREA validation failed: {validation_result['errors']}")
        
        self.assertEqual(commarea['CDEMO-USER-ID'], user_id.upper())
        self.assertEqual(commarea['CDEMO-USER-TYPE'], 'A')
        self.assertEqual(commarea['CDEMO-FROM-TRANID'], 'CC00')
        self.assertEqual(commarea['CDEMO-FROM-PROGRAM'], 'COSGN00C')
        self.assertEqual(commarea['CDEMO-PGM-CONTEXT'], 0)
        
        screen_state = self.simulator.get_screen_state()
        screen_validation = self.screen_validator.validate_successful_login_screen(screen_state)
        
        self.assertTrue(screen_validation['valid'], 
                       f"Screen validation failed: {screen_validation['errors']}")
    
    def test_regular_user_valid_login(self):
        user_id = 'USER0001'
        password = 'PASSWORD'
        
        self.logger.info(f"Testing regular user login: {user_id}")
        
        result = self.simulator.simulate_login_transaction(user_id, password)
        
        self.assertTrue(result['success'], f"Login should succeed for {user_id}")
        self.assertEqual(result['target_program'], 'COMEN01C', 
                        "Regular user should transfer to COMEN01C")
        self.assertEqual(result['error_message'], '', 
                        "Successful login should have no error message")
        
        commarea = result['commarea']
        validation_result = self.commarea_validator.validate_regular_user_flow(commarea, user_id)
        
        self.assertTrue(validation_result['valid'], 
                       f"COMMAREA validation failed: {validation_result['errors']}")
        
        self.assertEqual(commarea['CDEMO-USER-ID'], user_id.upper())
        self.assertEqual(commarea['CDEMO-USER-TYPE'], 'U')
        self.assertEqual(commarea['CDEMO-FROM-TRANID'], 'CC00')
        self.assertEqual(commarea['CDEMO-FROM-PROGRAM'], 'COSGN00C')
        self.assertEqual(commarea['CDEMO-PGM-CONTEXT'], 0)
        
        screen_state = self.simulator.get_screen_state()
        screen_validation = self.screen_validator.validate_successful_login_screen(screen_state)
        
        self.assertTrue(screen_validation['valid'], 
                       f"Screen validation failed: {screen_validation['errors']}")
    
    def test_case_insensitive_login(self):
        test_cases = [
            ('admin001', 'password'),
            ('ADMIN001', 'PASSWORD'),
            ('Admin001', 'Password'),
            ('user0001', 'password'),
            ('USER0001', 'PASSWORD'),
            ('User0001', 'Password')
        ]
        
        for user_id, password in test_cases:
            with self.subTest(user_id=user_id, password=password):
                self.logger.info(f"Testing case insensitive login: {user_id}/{password}")
                
                result = self.simulator.simulate_login_transaction(user_id, password)
                
                self.assertTrue(result['success'], 
                               f"Login should succeed for {user_id} (case insensitive)")
                
                expected_program = 'COADM01C' if user_id.upper().startswith('ADMIN') else 'COMEN01C'
                self.assertEqual(result['target_program'], expected_program)
                
                commarea = result['commarea']
                self.assertEqual(commarea['CDEMO-USER-ID'], user_id.upper())
    
    def test_commarea_field_population(self):
        user_id = 'ADMIN001'
        password = 'PASSWORD'
        
        result = self.simulator.simulate_login_transaction(user_id, password)
        commarea = result['commarea']
        
        required_fields = [
            'CDEMO-FROM-TRANID',
            'CDEMO-FROM-PROGRAM',
            'CDEMO-USER-ID',
            'CDEMO-USER-TYPE',
            'CDEMO-PGM-CONTEXT'
        ]
        
        for field in required_fields:
            self.assertIn(field, commarea, f"COMMAREA should contain {field}")
            self.assertIsNotNone(commarea[field], f"COMMAREA field {field} should not be None")
        
        self.assertEqual(commarea['CDEMO-FROM-TRANID'], 'CC00')
        self.assertEqual(commarea['CDEMO-FROM-PROGRAM'], 'COSGN00C')
        self.assertEqual(commarea['CDEMO-USER-ID'], 'ADMIN001')
        self.assertEqual(commarea['CDEMO-USER-TYPE'], 'A')
        self.assertEqual(commarea['CDEMO-PGM-CONTEXT'], 0)
    
    def test_user_type_determination(self):
        test_cases = [
            ('ADMIN001', 'A', 'COADM01C'),
            ('USER0001', 'U', 'COMEN01C'),
            ('TESTUSER', 'U', 'COMEN01C'),
            ('EDGECASE', 'U', 'COMEN01C')
        ]
        
        for user_id, expected_type, expected_program in test_cases:
            with self.subTest(user_id=user_id):
                result = self.simulator.simulate_login_transaction(user_id, 'PASSWORD' if user_id.startswith('ADMIN') or user_id == 'USER0001' else 'TESTPASS' if user_id == 'TESTUSER' else '12345678')
                
                self.assertTrue(result['success'], f"Login should succeed for {user_id}")
                
                commarea = result['commarea']
                self.assertEqual(commarea['CDEMO-USER-TYPE'], expected_type,
                               f"User type should be {expected_type} for {user_id}")
                self.assertEqual(result['target_program'], expected_program,
                               f"Target program should be {expected_program} for {user_id}")
    
    def test_vsam_file_read_success(self):
        user_id = 'USER0001'
        password = 'PASSWORD'
        
        result = self.simulator.simulate_login_transaction(user_id, password)
        
        self.assertTrue(result['success'])
        self.assertEqual(result['resp_code'].value, 0, "VSAM read should return RESP code 0 for valid user")
    
    def test_multiple_successful_logins(self):
        users = [
            ('ADMIN001', 'PASSWORD'),
            ('USER0001', 'PASSWORD'),
            ('TESTUSER', 'TESTPASS'),
            ('EDGECASE', '12345678')
        ]
        
        for user_id, password in users:
            with self.subTest(user_id=user_id):
                result = self.simulator.simulate_login_transaction(user_id, password)
                
                self.assertTrue(result['success'], f"Login should succeed for {user_id}")
                self.assertEqual(result['error_message'], '')
                
                commarea = result['commarea']
                self.assertEqual(commarea['CDEMO-USER-ID'], user_id.upper())
                
                if user_id.startswith('ADMIN'):
                    self.assertEqual(commarea['CDEMO-USER-TYPE'], 'A')
                    self.assertEqual(result['target_program'], 'COADM01C')
                else:
                    self.assertEqual(commarea['CDEMO-USER-TYPE'], 'U')
                    self.assertEqual(result['target_program'], 'COMEN01C')

if __name__ == '__main__':
    unittest.main()
