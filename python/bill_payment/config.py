from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    api_host: str = "0.0.0.0"
    api_port: int = 8000
    environment: str = "development"
    
    class Config:
        env_file = ".env"


settings = Settings()
