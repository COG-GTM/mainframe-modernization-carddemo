from typing import Dict, Any, Tuple, Optional
import logging

class ScreenValidator:
    def __init__(self):
        self.logger = logging.getLogger(__name__)
    
    def validate_cursor_positioning(self, screen_state: Dict[str, Any], expected_field: str) -> Dict[str, Any]:
        validation_result = {
            'valid': True,
            'errors': [],
            'warnings': []
        }
        
        cursor_field = screen_state.get('cursor_field', '')
        
        if cursor_field != expected_field:
            validation_result['errors'].append(
                f"Cursor position mismatch: expected '{expected_field}', got '{cursor_field}'"
            )
            validation_result['valid'] = False
        
        fields = screen_state.get('fields', {})
        if expected_field in fields:
            field_info = fields[expected_field]
            cursor_position = field_info.get('cursor_position', 0)
            
            if cursor_position != -1:
                validation_result['errors'].append(
                    f"Field {expected_field} cursor position should be -1, got {cursor_position}"
                )
                validation_result['valid'] = False
        
        return validation_result
    
    def validate_error_message(self, screen_state: Dict[str, Any], expected_message: str) -> Dict[str, Any]:
        validation_result = {
            'valid': True,
            'errors': [],
            'warnings': []
        }
        
        actual_message = screen_state.get('error_message', '')
        
        if actual_message != expected_message:
            validation_result['errors'].append(
                f"Error message mismatch: expected '{expected_message}', got '{actual_message}'"
            )
            validation_result['valid'] = False
        
        fields = screen_state.get('fields', {})
        if 'ERRMSG' in fields:
            errmsg_field = fields['ERRMSG']
            field_value = errmsg_field.get('value', '')
            
            if field_value != expected_message:
                validation_result['errors'].append(
                    f"ERRMSG field value mismatch: expected '{expected_message}', got '{field_value}'"
                )
                validation_result['valid'] = False
            
            field_color = errmsg_field.get('color', '')
            if field_color != 'RED':
                validation_result['warnings'].append(
                    f"ERRMSG field color should be RED, got '{field_color}'"
                )
            
            field_position = errmsg_field.get('position', (0, 0))
            if field_position != (23, 1):
                validation_result['errors'].append(
                    f"ERRMSG field position should be (23, 1), got {field_position}"
                )
                validation_result['valid'] = False
        
        return validation_result
    
    def validate_field_attributes(self, screen_state: Dict[str, Any], field_name: str, expected_attributes: Dict[str, Any]) -> Dict[str, Any]:
        validation_result = {
            'valid': True,
            'errors': [],
            'warnings': []
        }
        
        fields = screen_state.get('fields', {})
        
        if field_name not in fields:
            validation_result['errors'].append(f"Field {field_name} not found in screen state")
            validation_result['valid'] = False
            return validation_result
        
        field_info = fields[field_name]
        
        for attr_name, expected_value in expected_attributes.items():
            actual_value = field_info.get(attr_name)
            
            if actual_value != expected_value:
                validation_result['errors'].append(
                    f"Field {field_name} attribute {attr_name}: expected {expected_value}, got {actual_value}"
                )
                validation_result['valid'] = False
        
        return validation_result
    
    def validate_userid_field(self, screen_state: Dict[str, Any]) -> Dict[str, Any]:
        expected_attributes = {
            'position': (19, 43),
            'color': 'GREEN'
        }
        return self.validate_field_attributes(screen_state, 'USERID', expected_attributes)
    
    def validate_passwd_field(self, screen_state: Dict[str, Any]) -> Dict[str, Any]:
        expected_attributes = {
            'position': (20, 43),
            'color': 'GREEN'
        }
        return self.validate_field_attributes(screen_state, 'PASSWD', expected_attributes)
    
    def validate_screen_layout(self, screen_state: Dict[str, Any]) -> Dict[str, Any]:
        validation_result = {
            'valid': True,
            'errors': [],
            'warnings': []
        }
        
        map_name = screen_state.get('map_name', '')
        if map_name != 'COSGN0A':
            validation_result['errors'].append(
                f"Expected map name 'COSGN0A', got '{map_name}'"
            )
            validation_result['valid'] = False
        
        required_fields = ['USERID', 'PASSWD', 'ERRMSG', 'TRNNAME', 'PGMNAME']
        fields = screen_state.get('fields', {})
        
        for field_name in required_fields:
            if field_name not in fields:
                validation_result['errors'].append(f"Required field {field_name} missing from screen")
                validation_result['valid'] = False
        
        userid_result = self.validate_userid_field(screen_state)
        validation_result['errors'].extend(userid_result['errors'])
        validation_result['warnings'].extend(userid_result['warnings'])
        
        passwd_result = self.validate_passwd_field(screen_state)
        validation_result['errors'].extend(passwd_result['errors'])
        validation_result['warnings'].extend(passwd_result['warnings'])
        
        if not all([userid_result['valid'], passwd_result['valid']]):
            validation_result['valid'] = False
        
        return validation_result
    
    def validate_field_length_constraints(self, screen_state: Dict[str, Any]) -> Dict[str, Any]:
        validation_result = {
            'valid': True,
            'errors': [],
            'warnings': []
        }
        
        fields = screen_state.get('fields', {})
        
        field_length_specs = {
            'USERID': 8,
            'PASSWD': 8,
            'ERRMSG': 78,
            'TRNNAME': 4,
            'PGMNAME': 8
        }
        
        for field_name, max_length in field_length_specs.items():
            if field_name in fields:
                field_value = fields[field_name].get('value', '')
                if len(field_value) > max_length:
                    validation_result['errors'].append(
                        f"Field {field_name} value exceeds maximum length {max_length}: '{field_value}'"
                    )
                    validation_result['valid'] = False
        
        return validation_result
    
    def validate_empty_field_error(self, screen_state: Dict[str, Any], field_name: str, expected_message: str) -> Dict[str, Any]:
        validation_result = {
            'valid': True,
            'errors': [],
            'warnings': []
        }
        
        cursor_result = self.validate_cursor_positioning(screen_state, field_name)
        validation_result['errors'].extend(cursor_result['errors'])
        validation_result['warnings'].extend(cursor_result['warnings'])
        
        message_result = self.validate_error_message(screen_state, expected_message)
        validation_result['errors'].extend(message_result['errors'])
        validation_result['warnings'].extend(message_result['warnings'])
        
        if not all([cursor_result['valid'], message_result['valid']]):
            validation_result['valid'] = False
        
        return validation_result
    
    def validate_authentication_error(self, screen_state: Dict[str, Any], error_type: str, expected_cursor_field: str) -> Dict[str, Any]:
        validation_result = {
            'valid': True,
            'errors': [],
            'warnings': []
        }
        
        error_messages = {
            'user_not_found': 'User not found. Try again ...',
            'wrong_password': 'Wrong Password. Try again ...',
            'unable_to_verify': 'Unable to verify the User ...'
        }
        
        expected_message = error_messages.get(error_type, '')
        if not expected_message:
            validation_result['errors'].append(f"Unknown error type: {error_type}")
            validation_result['valid'] = False
            return validation_result
        
        cursor_result = self.validate_cursor_positioning(screen_state, expected_cursor_field)
        validation_result['errors'].extend(cursor_result['errors'])
        validation_result['warnings'].extend(cursor_result['warnings'])
        
        message_result = self.validate_error_message(screen_state, expected_message)
        validation_result['errors'].extend(message_result['errors'])
        validation_result['warnings'].extend(message_result['warnings'])
        
        if not all([cursor_result['valid'], message_result['valid']]):
            validation_result['valid'] = False
        
        return validation_result
    
    def validate_successful_login_screen(self, screen_state: Dict[str, Any]) -> Dict[str, Any]:
        validation_result = {
            'valid': True,
            'errors': [],
            'warnings': []
        }
        
        error_message = screen_state.get('error_message', '')
        if error_message:
            validation_result['errors'].append(
                f"Successful login should have no error message, got: '{error_message}'"
            )
            validation_result['valid'] = False
        
        cursor_field = screen_state.get('cursor_field', '')
        if cursor_field:
            validation_result['warnings'].append(
                f"Successful login should not set cursor field, got: '{cursor_field}'"
            )
        
        return validation_result
