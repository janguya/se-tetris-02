# Logger 사용 가이드

## 개요

프로젝트에 간단한 로깅 유틸리티 클래스를 추가했습니다. 이를 통해 개발 중에는 로그를 보고, 프로덕션 빌드에서는 로그가 비활성화할 수 있는 옵션을 제공.

## 사용 방법

### 1. Logger 클래스 import

```java
import com.example.utils.Logger;
```

### 2. 로그 레벨별 사용

#### INFO 로그 (일반 정보)

```java
Logger.info("게임 시작");
Logger.info("점수: %d, 레벨: %d", score, level);
```

#### ERROR 로그 (에러 - 항상 출력됨)

```java
Logger.error("파일을 찾을 수 없습니다");
Logger.error("예외 발생", exception);
```

## 개발 모드 / 프로덕션 모드 전환

### 개발 모드 (로그 활성화)

`Logger.java` 파일의 `DEBUG_MODE`를 `true`로 설정:

```java
private static final boolean DEBUG_MODE = true;
```

### 프로덕션 모드 (로그 비활성화)

`Logger.java` 파일의 `DEBUG_MODE`를 `false`로 설정:

```java
private static final boolean DEBUG_MODE = false;
```

**중요**: 패키징 전에 반드시 `DEBUG_MODE = false`로 설정하세요!

## 예시: 기존 코드 변경

### Before (기존)

```java
System.out.println("✓ Settings saved to: " + SETTINGS_PATH);
System.out.println(">>> ItemManager: Item should spawn!");
System.err.println("Failed to save: " + e.getMessage());
e.printStackTrace();
```

### After (변경)

```java
Logger.info("Settings saved to: %s", SETTINGS_PATH);
Logger.debug("ItemManager: Item should spawn!");
Logger.error("Failed to save: " + e.getMessage(), e);
```
