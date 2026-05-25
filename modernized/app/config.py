from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    DATABASE_URL: str = "postgresql://carddemo:carddemo@localhost:5432/carddemo"

    model_config = {"env_file": ".env", "env_file_encoding": "utf-8"}


settings = Settings()
