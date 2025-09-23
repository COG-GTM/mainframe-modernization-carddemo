import logging
from typing import Dict, Any, Optional, Tuple
from dataclasses import dataclass
from enum import Enum

class CICSResponseCode(Enum):
    NORMAL = 0
    NOTFND = 13
    IOERR = 14
    INVREQ = 16

class AIDKey(Enum):
    ENTER = 'ENTER'
    PF3 = 'PF3'
    CLEAR = 'CLEAR'
    OTHER = 'OTHER'

@dataclass
class BMSField:
    name: str
    position: Tuple[int, int]
    length: int
    value: str = ""
    cursor_position: int = 0
    color: str = "GREEN"
    attributes: list = None
    
    def __post_init__(self):
        if self.attributes is None:
            self.attributes = []

@dataclass
class ScreenMap:
    name: str
    fields: Dict[str, BMSField]
    error_message: str = ""
    cursor_field: str = ""

class VSAMFile:
    def __init__(self, name: str):
        self.name = name
        self.records = {}
        self._setup_test_data()
    
    def _setup_test_data(self):
        self.records = {
            'ADMIN001': {
                'SEC-USR-ID': 'ADMIN001',
                'SEC-USR-FNAME': 'MARGARET',
                'SEC-USR-LNAME': 'GOLD',
                'SEC-USR-PWD': 'PASSWORD',
                'SEC-USR-TYPE': 'A',
                'SEC-USR-FILLER': ' ' * 23
            },
            'USER0001': {
                'SEC-USR-ID': 'USER0001',
                'SEC-USR-FNAME': 'LAWRENCE',
                'SEC-USR-LNAME': 'THOMAS',
                'SEC-USR-PWD': 'PASSWORD',
                'SEC-USR-TYPE': 'U',
                'SEC-USR-FILLER': ' ' * 23
            },
            'TESTUSER': {
                'SEC-USR-ID': 'TESTUSER',
                'SEC-USR-FNAME': 'TEST',
                'SEC-USR-LNAME': 'USER',
                'SEC-USR-PWD': 'TESTPASS',
                'SEC-USR-TYPE': 'U',
                'SEC-USR-FILLER': ' ' * 23
            },
            'EDGECASE': {
                'SEC-USR-ID': 'EDGECASE',
                'SEC-USR-FNAME': 'EDGE',
                'SEC-USR-LNAME': 'CASE',
                'SEC-USR-PWD': '12345678',
                'SEC-USR-TYPE': 'U',
                'SEC-USR-FILLER': ' ' * 23
            }
        }
    
    def read(self, key: str) -> Tuple[CICSResponseCode, Optional[Dict]]:
        key = key.upper().strip()
        if key in self.records:
            return CICSResponseCode.NORMAL, self.records[key].copy()
        else:
            return CICSResponseCode.NOTFND, None

class CICSSimulator:
    def __init__(self):
        self.logger = logging.getLogger(__name__)
        self.current_map = None
        self.commarea = {}
        self.vsam_files = {
            'USRSEC': VSAMFile('USRSEC')
        }
        self.last_aid_key = None
        self.transaction_id = 'CC00'
        self.program_name = 'COSGN00C'
        self._setup_login_screen()
    
    def _setup_login_screen(self):
        fields = {
            'USERID': BMSField(
                name='USERID',
                position=(19, 43),
                length=8,
                attributes=['UNPROT', 'FSET', 'IC', 'NORM'],
                color='GREEN'
            ),
            'PASSWD': BMSField(
                name='PASSWD',
                position=(20, 43),
                length=8,
                attributes=['UNPROT', 'FSET', 'DRK'],
                color='GREEN'
            ),
            'ERRMSG': BMSField(
                name='ERRMSG',
                position=(23, 1),
                length=78,
                attributes=['ASKIP', 'BRT', 'FSET'],
                color='RED'
            ),
            'TRNNAME': BMSField(
                name='TRNNAME',
                position=(1, 8),
                length=4,
                value='CC00',
                attributes=['ASKIP', 'FSET', 'NORM'],
                color='BLUE'
            ),
            'PGMNAME': BMSField(
                name='PGMNAME',
                position=(2, 8),
                length=8,
                value='COSGN00C',
                attributes=['FSET', 'NORM', 'PROT'],
                color='BLUE'
            )
        }
        
        self.current_map = ScreenMap(
            name='COSGN0A',
            fields=fields
        )
    
    def send_map(self, map_name: str, mapset: str, erase: bool = True, cursor: bool = True):
        if map_name == 'COSGN0A' and mapset == 'COSGN00':
            if cursor and self.current_map.cursor_field:
                field = self.current_map.fields.get(self.current_map.cursor_field)
                if field:
                    field.cursor_position = -1
        
        self.logger.info(f"Sent map {map_name} from mapset {mapset}")
        return True
    
    def receive_map(self, map_name: str, mapset: str) -> Tuple[CICSResponseCode, Dict[str, str]]:
        if map_name == 'COSGN0A' and mapset == 'COSGN00':
            input_data = {}
            for field_name, field in self.current_map.fields.items():
                if 'UNPROT' in field.attributes:
                    input_data[field_name + 'I'] = field.value
                    input_data[field_name + 'L'] = len(field.value)
            
            return CICSResponseCode.NORMAL, input_data
        
        return CICSResponseCode.INVREQ, {}
    
    def set_field_value(self, field_name: str, value: str):
        if field_name in self.current_map.fields:
            self.current_map.fields[field_name].value = value[:self.current_map.fields[field_name].length]
    
    def get_field_value(self, field_name: str) -> str:
        if field_name in self.current_map.fields:
            return self.current_map.fields[field_name].value
        return ""
    
    def set_cursor_position(self, field_name: str):
        self.current_map.cursor_field = field_name
        if field_name in self.current_map.fields:
            self.current_map.fields[field_name].cursor_position = -1
    
    def set_error_message(self, message: str):
        self.current_map.error_message = message
        if 'ERRMSG' in self.current_map.fields:
            self.current_map.fields['ERRMSG'].value = message
    
    def clear_error_message(self):
        self.current_map.error_message = ""
        if 'ERRMSG' in self.current_map.fields:
            self.current_map.fields['ERRMSG'].value = ""
    
    def press_key(self, aid_key: AIDKey):
        self.last_aid_key = aid_key
    
    def read_vsam_file(self, file_name: str, key: str) -> Tuple[CICSResponseCode, Optional[Dict]]:
        if file_name in self.vsam_files:
            return self.vsam_files[file_name].read(key)
        return CICSResponseCode.INVREQ, None
    
    def initialize_commarea(self):
        self.commarea = {
            'CDEMO-FROM-TRANID': '',
            'CDEMO-FROM-PROGRAM': '',
            'CDEMO-TO-TRANID': '',
            'CDEMO-TO-PROGRAM': '',
            'CDEMO-USER-ID': '',
            'CDEMO-USER-TYPE': '',
            'CDEMO-PGM-CONTEXT': 0,
            'CDEMO-CUST-ID': 0,
            'CDEMO-CUST-FNAME': '',
            'CDEMO-CUST-MNAME': '',
            'CDEMO-CUST-LNAME': '',
            'CDEMO-ACCT-ID': 0,
            'CDEMO-ACCT-STATUS': '',
            'CDEMO-CARD-NUM': 0,
            'CDEMO-LAST-MAP': '',
            'CDEMO-LAST-MAPSET': ''
        }
    
    def set_commarea_field(self, field_name: str, value: Any):
        if field_name in self.commarea:
            self.commarea[field_name] = value
    
    def get_commarea_field(self, field_name: str) -> Any:
        return self.commarea.get(field_name)
    
    def transfer_control(self, program_name: str) -> str:
        self.logger.info(f"Transferring control to program {program_name}")
        return program_name
    
    def simulate_login_transaction(self, user_id: str, password: str, aid_key: AIDKey = AIDKey.ENTER) -> Dict[str, Any]:
        self.initialize_commarea()
        self.clear_error_message()
        
        result = {
            'success': False,
            'error_message': '',
            'cursor_field': '',
            'target_program': '',
            'commarea': {},
            'resp_code': CICSResponseCode.NORMAL
        }
        
        self.press_key(aid_key)
        
        if aid_key == AIDKey.PF3:
            result['error_message'] = 'Thank you for using CardDemo application'
            result['success'] = True
            return result
        
        if aid_key != AIDKey.ENTER:
            result['error_message'] = 'Invalid key pressed'
            result['cursor_field'] = 'USERID'
            return result
        
        self.set_field_value('USERID', user_id)
        self.set_field_value('PASSWD', password)
        
        if not user_id or user_id.isspace():
            result['error_message'] = 'Please enter User ID ...'
            result['cursor_field'] = 'USERID'
            self.set_cursor_position('USERID')
            self.set_error_message(result['error_message'])
            return result
        
        if not password or password.isspace():
            result['error_message'] = 'Please enter Password ...'
            result['cursor_field'] = 'PASSWD'
            self.set_cursor_position('PASSWD')
            self.set_error_message(result['error_message'])
            return result
        
        user_id_trimmed = user_id.strip().upper()
        password_trimmed = password.strip().upper()
        
        resp_code, user_record = self.read_vsam_file('USRSEC', user_id_trimmed)
        result['resp_code'] = resp_code
        
        if resp_code == CICSResponseCode.NOTFND:
            result['error_message'] = 'User not found. Try again ...'
            result['cursor_field'] = 'USERID'
            self.set_cursor_position('USERID')
            self.set_error_message(result['error_message'])
            return result
        
        if resp_code != CICSResponseCode.NORMAL:
            result['error_message'] = 'Unable to verify the User ...'
            result['cursor_field'] = 'USERID'
            self.set_cursor_position('USERID')
            self.set_error_message(result['error_message'])
            return result
        
        if user_record['SEC-USR-PWD'] != password_trimmed:
            result['error_message'] = 'Wrong Password. Try again ...'
            result['cursor_field'] = 'PASSWD'
            self.set_cursor_position('PASSWD')
            self.set_error_message(result['error_message'])
            return result
        
        self.set_commarea_field('CDEMO-FROM-TRANID', self.transaction_id)
        self.set_commarea_field('CDEMO-FROM-PROGRAM', self.program_name)
        self.set_commarea_field('CDEMO-USER-ID', user_id_trimmed)
        self.set_commarea_field('CDEMO-USER-TYPE', user_record['SEC-USR-TYPE'])
        self.set_commarea_field('CDEMO-PGM-CONTEXT', 0)
        
        if user_record['SEC-USR-TYPE'] == 'A':
            result['target_program'] = 'COADM01C'
        else:
            result['target_program'] = 'COMEN01C'
        
        result['success'] = True
        result['commarea'] = self.commarea.copy()
        
        return result
    
    def get_screen_state(self) -> Dict[str, Any]:
        return {
            'map_name': self.current_map.name if self.current_map else '',
            'fields': {name: {
                'value': field.value,
                'cursor_position': field.cursor_position,
                'color': field.color,
                'position': field.position
            } for name, field in (self.current_map.fields.items() if self.current_map else {})},
            'error_message': self.current_map.error_message if self.current_map else '',
            'cursor_field': self.current_map.cursor_field if self.current_map else ''
        }
