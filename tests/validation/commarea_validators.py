from typing import Dict, Any, Optional
import logging

class COMMAreaValidator:
    def __init__(self):
        self.logger = logging.getLogger(__name__)
    
    def validate_commarea_structure(self, commarea: Dict[str, Any]) -> Dict[str, Any]:
        validation_result = {
            'valid': True,
            'errors': [],
            'warnings': []
        }
        
        required_fields = [
            'CDEMO-FROM-TRANID',
            'CDEMO-FROM-PROGRAM', 
            'CDEMO-TO-TRANID',
            'CDEMO-TO-PROGRAM',
            'CDEMO-USER-ID',
            'CDEMO-USER-TYPE',
            'CDEMO-PGM-CONTEXT'
        ]
        
        for field in required_fields:
            if field not in commarea:
                validation_result['errors'].append(f"Missing required field: {field}")
                validation_result['valid'] = False
        
        return validation_result
    
    def validate_user_authentication(self, commarea: Dict[str, Any], expected_user_id: str, expected_user_type: str) -> Dict[str, Any]:
        validation_result = {
            'valid': True,
            'errors': [],
            'warnings': []
        }
        
        actual_user_id = commarea.get('CDEMO-USER-ID', '')
        actual_user_type = commarea.get('CDEMO-USER-TYPE', '')
        
        if actual_user_id != expected_user_id.upper():
            validation_result['errors'].append(
                f"User ID mismatch: expected '{expected_user_id.upper()}', got '{actual_user_id}'"
            )
            validation_result['valid'] = False
        
        if actual_user_type != expected_user_type:
            validation_result['errors'].append(
                f"User type mismatch: expected '{expected_user_type}', got '{actual_user_type}'"
            )
            validation_result['valid'] = False
        
        return validation_result
    
    def validate_program_transfer(self, commarea: Dict[str, Any], expected_target_program: str) -> Dict[str, Any]:
        validation_result = {
            'valid': True,
            'errors': [],
            'warnings': []
        }
        
        from_tranid = commarea.get('CDEMO-FROM-TRANID', '')
        from_program = commarea.get('CDEMO-FROM-PROGRAM', '')
        
        if from_tranid != 'CC00':
            validation_result['errors'].append(
                f"FROM-TRANID should be 'CC00', got '{from_tranid}'"
            )
            validation_result['valid'] = False
        
        if from_program != 'COSGN00C':
            validation_result['errors'].append(
                f"FROM-PROGRAM should be 'COSGN00C', got '{from_program}'"
            )
            validation_result['valid'] = False
        
        return validation_result
    
    def validate_admin_user_flow(self, commarea: Dict[str, Any], user_id: str) -> Dict[str, Any]:
        validation_result = self.validate_user_authentication(commarea, user_id, 'A')
        
        if validation_result['valid']:
            transfer_result = self.validate_program_transfer(commarea, 'COADM01C')
            validation_result['errors'].extend(transfer_result['errors'])
            validation_result['warnings'].extend(transfer_result['warnings'])
            if not transfer_result['valid']:
                validation_result['valid'] = False
        
        return validation_result
    
    def validate_regular_user_flow(self, commarea: Dict[str, Any], user_id: str) -> Dict[str, Any]:
        validation_result = self.validate_user_authentication(commarea, user_id, 'U')
        
        if validation_result['valid']:
            transfer_result = self.validate_program_transfer(commarea, 'COMEN01C')
            validation_result['errors'].extend(transfer_result['errors'])
            validation_result['warnings'].extend(transfer_result['warnings'])
            if not transfer_result['valid']:
                validation_result['valid'] = False
        
        return validation_result
    
    def validate_program_context(self, commarea: Dict[str, Any], expected_context: int = 0) -> Dict[str, Any]:
        validation_result = {
            'valid': True,
            'errors': [],
            'warnings': []
        }
        
        actual_context = commarea.get('CDEMO-PGM-CONTEXT', -1)
        
        if actual_context != expected_context:
            validation_result['errors'].append(
                f"Program context mismatch: expected {expected_context}, got {actual_context}"
            )
            validation_result['valid'] = False
        
        return validation_result
    
    def validate_field_lengths(self, commarea: Dict[str, Any]) -> Dict[str, Any]:
        validation_result = {
            'valid': True,
            'errors': [],
            'warnings': []
        }
        
        field_length_specs = {
            'CDEMO-FROM-TRANID': 4,
            'CDEMO-FROM-PROGRAM': 8,
            'CDEMO-TO-TRANID': 4,
            'CDEMO-TO-PROGRAM': 8,
            'CDEMO-USER-ID': 8,
            'CDEMO-USER-TYPE': 1,
            'CDEMO-LAST-MAP': 7,
            'CDEMO-LAST-MAPSET': 7
        }
        
        for field_name, max_length in field_length_specs.items():
            if field_name in commarea:
                field_value = str(commarea[field_name])
                if len(field_value) > max_length:
                    validation_result['errors'].append(
                        f"Field {field_name} exceeds maximum length {max_length}: '{field_value}'"
                    )
                    validation_result['valid'] = False
        
        return validation_result
    
    def validate_user_type_flags(self, commarea: Dict[str, Any]) -> Dict[str, Any]:
        validation_result = {
            'valid': True,
            'errors': [],
            'warnings': []
        }
        
        user_type = commarea.get('CDEMO-USER-TYPE', '')
        
        if user_type not in ['A', 'U']:
            validation_result['errors'].append(
                f"Invalid user type: '{user_type}'. Must be 'A' (admin) or 'U' (user)"
            )
            validation_result['valid'] = False
        
        return validation_result
    
    def validate_complete_login_flow(self, commarea: Dict[str, Any], user_id: str, expected_user_type: str, expected_target_program: str) -> Dict[str, Any]:
        validation_result = {
            'valid': True,
            'errors': [],
            'warnings': []
        }
        
        structure_result = self.validate_commarea_structure(commarea)
        validation_result['errors'].extend(structure_result['errors'])
        validation_result['warnings'].extend(structure_result['warnings'])
        
        if not structure_result['valid']:
            validation_result['valid'] = False
            return validation_result
        
        auth_result = self.validate_user_authentication(commarea, user_id, expected_user_type)
        validation_result['errors'].extend(auth_result['errors'])
        validation_result['warnings'].extend(auth_result['warnings'])
        
        transfer_result = self.validate_program_transfer(commarea, expected_target_program)
        validation_result['errors'].extend(transfer_result['errors'])
        validation_result['warnings'].extend(transfer_result['warnings'])
        
        context_result = self.validate_program_context(commarea, 0)
        validation_result['errors'].extend(context_result['errors'])
        validation_result['warnings'].extend(context_result['warnings'])
        
        length_result = self.validate_field_lengths(commarea)
        validation_result['errors'].extend(length_result['errors'])
        validation_result['warnings'].extend(length_result['warnings'])
        
        type_result = self.validate_user_type_flags(commarea)
        validation_result['errors'].extend(type_result['errors'])
        validation_result['warnings'].extend(type_result['warnings'])
        
        if not all([auth_result['valid'], transfer_result['valid'], context_result['valid'], 
                   length_result['valid'], type_result['valid']]):
            validation_result['valid'] = False
        
        return validation_result
