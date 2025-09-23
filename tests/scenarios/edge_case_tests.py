import unittest
import logging
import time
import sys
import os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))
from framework.cics_simulator import CICSSimulator, AIDKey
from validation.commarea_validators import COMMAreaValidator
from validation.screen_validators import ScreenValidator

class EdgeCaseTests(unittest.TestCase):
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
    
    def test_f3_key_functionality(self):
        user_id = 'USER0001'
        password = 'PASSWORD'
        expected_message = 'Thank you for using CardDemo application'
        
        self.logger.info("Testing F3 key functionality")
        
        result = self.simulator.simulate_login_transaction(user_id, password, AIDKey.PF3)
        
        self.assertTrue(result['success'], "F3 key should be handled successfully")
        self.assertEqual(result['error_message'], expected_message)
        self.assertEqual(result['target_program'], '')
        self.assertEqual(result['cursor_field'], '')
    
    def test_invalid_aid_key(self):
        user_id = 'USER0001'
        password = 'PASSWORD'
        expected_message = 'Invalid key pressed'
        
        self.logger.info("Testing invalid AID key")
        
        result = self.simulator.simulate_login_transaction(user_id, password, AIDKey.OTHER)
        
        self.assertFalse(result['success'], "Invalid key should cause failure")
        self.assertEqual(result['error_message'], expected_message)
        self.assertEqual(result['cursor_field'], 'USERID')
        self.assertEqual(result['target_program'], '')
    
    def test_clear_key_functionality(self):
        user_id = 'USER0001'
        password = 'PASSWORD'
        expected_message = 'Invalid key pressed'
        
        self.logger.info("Testing CLEAR key functionality")
        
        result = self.simulator.simulate_login_transaction(user_id, password, AIDKey.CLEAR)
        
        self.assertFalse(result['success'], "CLEAR key should cause failure")
        self.assertEqual(result['error_message'], expected_message)
        self.assertEqual(result['cursor_field'], 'USERID')
    
    def test_maximum_length_userid(self):
        user_id = 'EDGECASE'
        password = '12345678'
        
        self.logger.info("Testing maximum length USERID (8 characters)")
        
        result = self.simulator.simulate_login_transaction(user_id, password)
        
        self.assertTrue(result['success'], "8-character USERID should work")
        self.assertEqual(result['target_program'], 'COMEN01C')
        
        commarea = result['commarea']
        self.assertEqual(commarea['CDEMO-USER-ID'], user_id.upper())
        self.assertEqual(len(commarea['CDEMO-USER-ID']), 8)
    
    def test_maximum_length_password(self):
        user_id = 'EDGECASE'
        password = '12345678'
        
        self.logger.info("Testing maximum length PASSWORD (8 characters)")
        
        result = self.simulator.simulate_login_transaction(user_id, password)
        
        self.assertTrue(result['success'], "8-character PASSWORD should work")
        self.assertEqual(result['target_program'], 'COMEN01C')
    
    def test_field_length_truncation(self):
        user_id = 'EDGECASE123456789'
        password = 'PASSWORD123456789'
        
        self.logger.info("Testing field length truncation")
        
        self.simulator.set_field_value('USERID', user_id)
        self.simulator.set_field_value('PASSWD', password)
        
        userid_value = self.simulator.get_field_value('USERID')
        passwd_value = self.simulator.get_field_value('PASSWD')
        
        self.assertEqual(len(userid_value), 8, "USERID should be truncated to 8 characters")
        self.assertEqual(len(passwd_value), 8, "PASSWORD should be truncated to 8 characters")
        self.assertEqual(userid_value, 'EDGECASE')
        self.assertEqual(passwd_value, 'PASSWORD')
    
    def test_special_characters_in_userid(self):
        special_char_users = [
            'USER@001',
            'USER#001',
            'USER$001',
            'USER%001'
        ]
        
        for user_id in special_char_users:
            with self.subTest(user_id=user_id):
                self.logger.info(f"Testing special characters in USERID: {user_id}")
                
                result = self.simulator.simulate_login_transaction(user_id, 'PASSWORD')
                
                self.assertFalse(result['success'], f"Login should fail for special character USERID: {user_id}")
                self.assertEqual(result['error_message'], 'User not found. Try again ...')
                self.assertEqual(result['cursor_field'], 'USERID')
    
    def test_special_characters_in_password(self):
        user_id = 'USER0001'
        special_passwords = [
            'PASS@123',
            'PASS#123',
            'PASS$123',
            'PASS%123'
        ]
        
        for password in special_passwords:
            with self.subTest(password=password):
                self.logger.info(f"Testing special characters in PASSWORD: {password}")
                
                result = self.simulator.simulate_login_transaction(user_id, password)
                
                self.assertFalse(result['success'], f"Login should fail for special character PASSWORD: {password}")
                self.assertEqual(result['error_message'], 'Wrong Password. Try again ...')
                self.assertEqual(result['cursor_field'], 'PASSWD')
    
    def test_numeric_userid(self):
        numeric_users = [
            '12345678',
            '00000001',
            '99999999'
        ]
        
        for user_id in numeric_users:
            with self.subTest(user_id=user_id):
                self.logger.info(f"Testing numeric USERID: {user_id}")
                
                result = self.simulator.simulate_login_transaction(user_id, 'PASSWORD')
                
                self.assertFalse(result['success'], f"Login should fail for numeric USERID: {user_id}")
                self.assertEqual(result['error_message'], 'User not found. Try again ...')
    
    def test_numeric_password(self):
        user_id = 'EDGECASE'
        password = '12345678'
        
        self.logger.info(f"Testing numeric PASSWORD: {password}")
        
        result = self.simulator.simulate_login_transaction(user_id, password)
        
        self.assertTrue(result['success'], "Numeric password should work for EDGECASE user")
        self.assertEqual(result['target_program'], 'COMEN01C')
    
    def test_leading_trailing_spaces(self):
        test_cases = [
            (' USER0001', 'PASSWORD'),
            ('USER0001 ', 'PASSWORD'),
            (' USER0001 ', 'PASSWORD'),
            ('USER0001', ' PASSWORD'),
            ('USER0001', 'PASSWORD '),
            ('USER0001', ' PASSWORD ')
        ]
        
        for user_id, password in test_cases:
            with self.subTest(user_id=repr(user_id), password=repr(password)):
                self.logger.info(f"Testing leading/trailing spaces: '{user_id}'/'{password}'")
                
                result = self.simulator.simulate_login_transaction(user_id, password)
                
                if user_id.strip() == 'USER0001' and password.strip() == 'PASSWORD':
                    self.assertTrue(result['success'], "Login should succeed after trimming spaces")
                    self.assertEqual(result['target_program'], 'COMEN01C')
                else:
                    self.assertFalse(result['success'], "Login should fail with incorrect credentials after trimming")
    
    def test_case_sensitivity_edge_cases(self):
        test_cases = [
            ('user0001', 'password'),
            ('User0001', 'Password'),
            ('uSeR0001', 'pAsSwOrD'),
            ('admin001', 'password'),
            ('Admin001', 'Password'),
            ('aDmIn001', 'pAsSwOrD')
        ]
        
        for user_id, password in test_cases:
            with self.subTest(user_id=user_id, password=password):
                self.logger.info(f"Testing case sensitivity: {user_id}/{password}")
                
                result = self.simulator.simulate_login_transaction(user_id, password)
                
                self.assertTrue(result['success'], f"Login should succeed with case variations: {user_id}")
                
                commarea = result['commarea']
                self.assertEqual(commarea['CDEMO-USER-ID'], user_id.upper())
                
                if user_id.upper().startswith('ADMIN'):
                    self.assertEqual(result['target_program'], 'COADM01C')
                    self.assertEqual(commarea['CDEMO-USER-TYPE'], 'A')
                else:
                    self.assertEqual(result['target_program'], 'COMEN01C')
                    self.assertEqual(commarea['CDEMO-USER-TYPE'], 'U')
    
    def test_screen_field_validation(self):
        user_id = 'USER0001'
        password = 'WRONGPWD'
        
        result = self.simulator.simulate_login_transaction(user_id, password)
        
        screen_state = self.simulator.get_screen_state()
        
        layout_validation = self.screen_validator.validate_screen_layout(screen_state)
        self.assertTrue(layout_validation['valid'], 
                       f"Screen layout validation failed: {layout_validation['errors']}")
        
        length_validation = self.screen_validator.validate_field_length_constraints(screen_state)
        self.assertTrue(length_validation['valid'], 
                       f"Field length validation failed: {length_validation['errors']}")
    
    def test_multiple_error_scenarios_sequence(self):
        error_scenarios = [
            ('', 'PASSWORD', 'Please enter User ID ...', 'USERID'),
            ('USER0001', '', 'Please enter Password ...', 'PASSWD'),
            ('INVALID01', 'PASSWORD', 'User not found. Try again ...', 'USERID'),
            ('USER0001', 'WRONGPWD', 'Wrong Password. Try again ...', 'PASSWD')
        ]
        
        for user_id, password, expected_message, expected_cursor in error_scenarios:
            with self.subTest(user_id=user_id, password=password):
                result = self.simulator.simulate_login_transaction(user_id, password)
                
                self.assertFalse(result['success'])
                self.assertEqual(result['error_message'], expected_message)
                self.assertEqual(result['cursor_field'], expected_cursor)
                
                screen_state = self.simulator.get_screen_state()
                self.assertEqual(screen_state['error_message'], expected_message)
                self.assertEqual(screen_state['cursor_field'], expected_cursor)
    
    def test_performance_under_load(self):
        user_credentials = [
            ('ADMIN001', 'PASSWORD'),
            ('USER0001', 'PASSWORD'),
            ('TESTUSER', 'TESTPASS'),
            ('EDGECASE', '12345678')
        ]
        
        start_time = time.time()
        
        for i in range(10):
            for user_id, password in user_credentials:
                result = self.simulator.simulate_login_transaction(user_id, password)
                self.assertTrue(result['success'], f"Login should succeed for {user_id} in iteration {i}")
        
        elapsed_time = time.time() - start_time
        self.logger.info(f"Performance test completed in {elapsed_time:.2f} seconds for 40 logins")
        
        average_time = elapsed_time / 40
        self.assertLess(average_time, 1.0, "Average login time should be under 1 second")

if __name__ == '__main__':
    unittest.main()
