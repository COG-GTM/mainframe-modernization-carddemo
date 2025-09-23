import unittest
import logging
import time
import sys
import os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))
from framework.cics_simulator import CICSSimulator, AIDKey, CICSResponseCode
from validation.commarea_validators import COMMAreaValidator
from validation.screen_validators import ScreenValidator

class InvalidLoginTests(unittest.TestCase):
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
    
    def test_empty_userid_field(self):
        user_id = ''
        password = 'PASSWORD'
        expected_message = 'Please enter User ID ...'
        
        self.logger.info("Testing empty USERID field")
        
        result = self.simulator.simulate_login_transaction(user_id, password)
        
        self.assertFalse(result['success'], "Login should fail with empty USERID")
        self.assertEqual(result['error_message'], expected_message)
        self.assertEqual(result['cursor_field'], 'USERID')
        self.assertEqual(result['target_program'], '')
        
        screen_state = self.simulator.get_screen_state()
        validation_result = self.screen_validator.validate_empty_field_error(
            screen_state, 'USERID', expected_message
        )
        
        self.assertTrue(validation_result['valid'], 
                       f"Screen validation failed: {validation_result['errors']}")
    
    def test_spaces_only_userid_field(self):
        user_id = '        '
        password = 'PASSWORD'
        expected_message = 'Please enter User ID ...'
        
        self.logger.info("Testing spaces-only USERID field")
        
        result = self.simulator.simulate_login_transaction(user_id, password)
        
        self.assertFalse(result['success'], "Login should fail with spaces-only USERID")
        self.assertEqual(result['error_message'], expected_message)
        self.assertEqual(result['cursor_field'], 'USERID')
    
    def test_empty_password_field(self):
        user_id = 'USER0001'
        password = ''
        expected_message = 'Please enter Password ...'
        
        self.logger.info("Testing empty PASSWORD field")
        
        result = self.simulator.simulate_login_transaction(user_id, password)
        
        self.assertFalse(result['success'], "Login should fail with empty PASSWORD")
        self.assertEqual(result['error_message'], expected_message)
        self.assertEqual(result['cursor_field'], 'PASSWD')
        self.assertEqual(result['target_program'], '')
        
        screen_state = self.simulator.get_screen_state()
        validation_result = self.screen_validator.validate_empty_field_error(
            screen_state, 'PASSWD', expected_message
        )
        
        self.assertTrue(validation_result['valid'], 
                       f"Screen validation failed: {validation_result['errors']}")
    
    def test_spaces_only_password_field(self):
        user_id = 'USER0001'
        password = '        '
        expected_message = 'Please enter Password ...'
        
        self.logger.info("Testing spaces-only PASSWORD field")
        
        result = self.simulator.simulate_login_transaction(user_id, password)
        
        self.assertFalse(result['success'], "Login should fail with spaces-only PASSWORD")
        self.assertEqual(result['error_message'], expected_message)
        self.assertEqual(result['cursor_field'], 'PASSWD')
    
    def test_invalid_user_not_found(self):
        user_id = 'INVALID01'
        password = 'PASSWORD'
        expected_message = 'User not found. Try again ...'
        
        self.logger.info(f"Testing invalid user: {user_id}")
        
        result = self.simulator.simulate_login_transaction(user_id, password)
        
        self.assertFalse(result['success'], "Login should fail for invalid user")
        self.assertEqual(result['error_message'], expected_message)
        self.assertEqual(result['cursor_field'], 'USERID')
        self.assertEqual(result['target_program'], '')
        self.assertEqual(result['resp_code'], CICSResponseCode.NOTFND)
        
        screen_state = self.simulator.get_screen_state()
        validation_result = self.screen_validator.validate_authentication_error(
            screen_state, 'user_not_found', 'USERID'
        )
        
        self.assertTrue(validation_result['valid'], 
                       f"Screen validation failed: {validation_result['errors']}")
    
    def test_wrong_password(self):
        user_id = 'USER0001'
        password = 'WRONGPWD'
        expected_message = 'Wrong Password. Try again ...'
        
        self.logger.info(f"Testing wrong password for user: {user_id}")
        
        result = self.simulator.simulate_login_transaction(user_id, password)
        
        self.assertFalse(result['success'], "Login should fail with wrong password")
        self.assertEqual(result['error_message'], expected_message)
        self.assertEqual(result['cursor_field'], 'PASSWD')
        self.assertEqual(result['target_program'], '')
        self.assertEqual(result['resp_code'], CICSResponseCode.NORMAL)
        
        screen_state = self.simulator.get_screen_state()
        validation_result = self.screen_validator.validate_authentication_error(
            screen_state, 'wrong_password', 'PASSWD'
        )
        
        self.assertTrue(validation_result['valid'], 
                       f"Screen validation failed: {validation_result['errors']}")
    
    def test_admin_user_wrong_password(self):
        user_id = 'ADMIN001'
        password = 'WRONGPWD'
        expected_message = 'Wrong Password. Try again ...'
        
        self.logger.info(f"Testing wrong password for admin user: {user_id}")
        
        result = self.simulator.simulate_login_transaction(user_id, password)
        
        self.assertFalse(result['success'], "Admin login should fail with wrong password")
        self.assertEqual(result['error_message'], expected_message)
        self.assertEqual(result['cursor_field'], 'PASSWD')
        self.assertEqual(result['target_program'], '')
    
    def test_nonexistent_users(self):
        invalid_users = [
            'BADUSER1',
            'NOTFOUND',
            'FAKE0001',
            'INVALID2',
            'UNKNOWN1'
        ]
        
        for user_id in invalid_users:
            with self.subTest(user_id=user_id):
                self.logger.info(f"Testing nonexistent user: {user_id}")
                
                result = self.simulator.simulate_login_transaction(user_id, 'PASSWORD')
                
                self.assertFalse(result['success'], f"Login should fail for nonexistent user {user_id}")
                self.assertEqual(result['error_message'], 'User not found. Try again ...')
                self.assertEqual(result['cursor_field'], 'USERID')
                self.assertEqual(result['resp_code'], CICSResponseCode.NOTFND)
    
    def test_various_wrong_passwords(self):
        test_cases = [
            ('USER0001', 'WRONG'),
            ('USER0001', 'WRONGPWD'),
            ('USER0001', '12345678'),
            ('USER0001', 'PASSWORD1'),
            ('ADMIN001', 'ADMIN'),
            ('ADMIN001', 'WRONGPWD'),
            ('ADMIN001', 'PASSWORD1')
        ]
        
        for user_id, password in test_cases:
            with self.subTest(user_id=user_id, password=password):
                self.logger.info(f"Testing wrong password: {user_id}/{password}")
                
                result = self.simulator.simulate_login_transaction(user_id, password)
                
                self.assertFalse(result['success'], f"Login should fail for {user_id} with wrong password")
                self.assertEqual(result['error_message'], 'Wrong Password. Try again ...')
                self.assertEqual(result['cursor_field'], 'PASSWD')
    
    def test_both_fields_empty(self):
        user_id = ''
        password = ''
        expected_message = 'Please enter User ID ...'
        
        self.logger.info("Testing both USERID and PASSWORD empty")
        
        result = self.simulator.simulate_login_transaction(user_id, password)
        
        self.assertFalse(result['success'], "Login should fail with both fields empty")
        self.assertEqual(result['error_message'], expected_message)
        self.assertEqual(result['cursor_field'], 'USERID')
    
    def test_cursor_positioning_validation(self):
        test_cases = [
            ('', 'PASSWORD', 'USERID'),
            ('USER0001', '', 'PASSWD'),
            ('INVALID01', 'PASSWORD', 'USERID'),
            ('USER0001', 'WRONGPWD', 'PASSWD')
        ]
        
        for user_id, password, expected_cursor_field in test_cases:
            with self.subTest(user_id=user_id, password=password):
                result = self.simulator.simulate_login_transaction(user_id, password)
                
                self.assertFalse(result['success'])
                self.assertEqual(result['cursor_field'], expected_cursor_field)
                
                screen_state = self.simulator.get_screen_state()
                fields = screen_state.get('fields', {})
                
                if expected_cursor_field in fields:
                    cursor_position = fields[expected_cursor_field].get('cursor_position', 0)
                    self.assertEqual(cursor_position, -1, 
                                   f"Cursor position should be -1 for field {expected_cursor_field}")
    
    def test_error_message_display_attributes(self):
        user_id = 'INVALID01'
        password = 'PASSWORD'
        
        result = self.simulator.simulate_login_transaction(user_id, password)
        
        screen_state = self.simulator.get_screen_state()
        fields = screen_state.get('fields', {})
        
        self.assertIn('ERRMSG', fields, "ERRMSG field should be present")
        
        errmsg_field = fields['ERRMSG']
        self.assertEqual(errmsg_field.get('color'), 'RED', "Error message should be RED")
        self.assertEqual(errmsg_field.get('position'), (23, 1), "Error message should be at position (23,1)")
        self.assertEqual(len(errmsg_field.get('value', '')), len(result['error_message']))
    
    def test_no_commarea_population_on_failure(self):
        test_cases = [
            ('', 'PASSWORD'),
            ('USER0001', ''),
            ('INVALID01', 'PASSWORD'),
            ('USER0001', 'WRONGPWD')
        ]
        
        for user_id, password in test_cases:
            with self.subTest(user_id=user_id, password=password):
                result = self.simulator.simulate_login_transaction(user_id, password)
                
                self.assertFalse(result['success'])
                
                commarea = result.get('commarea', {})
                if commarea:
                    self.assertEqual(commarea.get('CDEMO-USER-ID', ''), '')
                    self.assertEqual(commarea.get('CDEMO-USER-TYPE', ''), '')

if __name__ == '__main__':
    unittest.main()
