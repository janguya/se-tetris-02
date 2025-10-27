# se-tetris-02

2025 SE 실습

## 프로젝트 구조

```
se-tetris-02/
├── demo/                  # JavaFX 테트리스 프로젝트
│   ├── src/               # 소스 코드
│   ├── pom.xml            # Maven 빌드 설정
│   ├── create-package.ps1 # 배포 패키지 생성 스크립트
│   └── icon.ico           # 애플리케이션 아이콘
└── README.md
```

## 빌드 및 실행

### 개발 모드 실행

```bash
cd demo
mvn javafx:run
```

### 테스트 실행

```bash
cd demo
mvn test
```

## 배포 패키지 생성

### 요구사항

- Java 17 이상
- Maven 3.6 이상
- PowerShell (Windows)
- ps2exe PowerShell 모듈

### ps2exe 설치 (최초 1회)

```powershell
Install-Module -Name ps2exe -Scope CurrentUser
```

### 배포 패키지 빌드

```bash
cd demo
mvn package -DskipTests
```

### 생성된 파일

빌드 완료 후 `demo/target/Tetris-Portable-1.0.zip` 파일이 생성됩니다 (~35 MB).

**ZIP 파일 구조:**

```
tetris-runtime/
├── Tetris.lnk          # 게임 실행 바로가기 (더블클릭)
├── README.txt          # 사용 설명서
└── bin/
    ├── Tetris.exe      # 게임 실행 파일
    ├── java.exe        # 내장 Java 런타임
    ├── *.dll           # 필요한 라이브러리
    └── ...
```
