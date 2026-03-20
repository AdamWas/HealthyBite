# HealthyBite - Dokumentacja

**Autorzy (GRUPA 5.1):**

* Karol Madej
* Paweł Przymęski
* Adam Wasielewski

## 1. Przegląd projektu

**HealthyBite** to aplikacja mobilna na system Android wspierająca użytkownika w monitorowaniu diety, nawodnienia oraz planowaniu zakupów spożywczych. Użytkownik może się zarejestrować i zalogować, a następnie zarządzać swoimi danymi żywieniowymi w ujęciu dziennym.

Aplikacja umożliwia:

* rejestrowanie posiłków,
* monitorowanie kalorii i makroskładników,
* śledzenie spożycia wody,
* zarządzanie listą zakupów,
* korzystanie z gotowych planów żywieniowych.

Dane przechowywane są w Google Cloud Firestore, a stan sesji użytkownika w Jetpack DataStore.

**Cel systemu:**
Ułatwienie użytkownikowi kontroli nad dietą i nawykami żywieniowymi poprzez centralizację funkcji w jednej aplikacji mobilnej.

## 2. Wymagania funkcjonalne

1. **Autoryzacja użytkownika**
System powinien umożliwiać użytkownikowi rejestrację, logowanie oraz wylogowanie, a także przechowywać aktualną sesję lokalnie.
2. **Zarządzanie posiłkami**
System powinien umożliwiać użytkownikowi dodawanie posiłków (własnych lub z szablonów), wyświetlanie dziennych posiłków oraz usuwanie wybranych wpisów.
3. **Panel dzienny (Dashboard)**
System powinien wyświetlać zagregowane dane dzienne, takie jak kalorie, makroskładniki oraz liczba posiłków.
4. **Śledzenie nawodnienia**
System powinien umożliwiać użytkownikowi rejestrowanie spożycia wody oraz wyświetlać postęp względem dziennego celu nawodnienia.
5. **Zarządzanie listą zakupów**
System powinien umożliwiać użytkownikowi dodawanie produktów, oznaczanie ich jako wykonane oraz usuwanie pozycji z listy.
6. **Plany żywieniowe**
System powinien umożliwiać użytkownikowi przeglądanie gotowych planów żywieniowych oraz zastosowanie planu do bieżącego dnia.
7. **Zarządzanie profilem użytkownika**
System powinien umożliwiać użytkownikowi przeglądanie danych profilu oraz modyfikację dziennego celu kalorycznego.

## 3. Wymagania niefunkcjonalne

1. **Wydajność i responsywność**
System powinien aktualizować interfejs użytkownika w sposób reaktywny przy użyciu Kotlin Flow oraz StateFlow, bez konieczności ręcznego odświeżania.
2. **Skalowalność i trwałość danych**
System powinien przechowywać dane użytkownika w bazie danych w chmurze (Firestore), umożliwiając dostęp z wielu urządzeń.
3. **Utrzymywalność kodu**
System powinien być oparty na architekturze MVVM oraz zachowywać separację odpowiedzialności pomiędzy warstwami aplikacji.

## 4. Użytkownicy docelowi (Target Users)

1. Osoby dbające o dietę i zdrowie.
2. Osoby planujące posiłki i zakupy.
3. Użytkownicy chcący kontrolować nawodnienie organizmu.

## 5. Korzyści biznesowe (Business Benefits)

1. Integracja wielu funkcji zdrowotnych w jednej aplikacji zwiększa zaangażowanie użytkownika.
2. Wykorzystanie Firestore umożliwia łatwe skalowanie systemu i dostęp z wielu urządzeń.
3. Gotowe dane startowe (seeding) ułatwiają szybkie rozpoczęcie korzystania z aplikacji.

## 6. Stack technologiczny
| Technologia        | Rola                   |
| ------------------ | ---------------------- |
| Kotlin             | Język programowania    |
| Jetpack Compose    | Warstwa UI             |
| Navigation Compose | Nawigacja              |
| Firebase Firestore | Baza danych            |
| DataStore          | Przechowywanie sesji   |
| Coroutines / Flow  | Obsługa asynchroniczna |
| Gradle             | System budowania       |

## 7. Architektura i komponenty

Aplikacja została zbudowana w oparciu o wzorzec MVVM (Model-View-ViewModel).

**Warstwy:**

* **UI** (`ui/`) – widoki i ViewModel
* **Domain** (`domain/`) – modele i interfejsy repozytoriów
* **Data** (`data/`) – implementacje repozytoriów oraz komunikacja z Firestore

**Kluczowe komponenty:**

* `HealthyBiteApplication` – inicjalizacja zależności
* `NavGraph` – zarządzanie nawigacją
* `MainScaffold` – struktura UI
* `FirestoreMealRepository` – operacje na posiłkach
* `FirestoreAuthRepository` – autoryzacja użytkownika

## 8. Opis klas (Class Overview / Diagram Description)

System składa się z następujących kluczowych klas:

**ViewModels:**

* `HomeViewModel`
* `LogViewModel`
* `AddMealViewModel`
* `ShoppingViewModel`
* `WaterViewModel`
* `PlansViewModel`
* `ProfileViewModel`

**Repositories:**

* `FirestoreMealRepository`
* `FirestoreAuthRepository`
* `FirestoreShoppingRepository`
* `FirestoreWaterRepository`
* `FirestorePlanRepository`

**Models:**

* `User`
* `MealEntry`
* `MealTemplate`
* `ShoppingItem`
* `PlanTemplate`

**Relacje:**

* ViewModel komunikuje się z Repository
* Repository komunikuje się z Firestore
* Modele reprezentują dane

## 9. Komunikacja z serwerem

Aplikacja wykorzystuje Google Cloud Firestore jako backend.

**Przykład komunikacji:**

**Metoda:**
`FirestoreMealRepository.observeEntriesForUserAndDate`

**Działanie:**

* zapytanie do kolekcji `mealEntries`
* użycie `addSnapshotListener`
* konwersja danych do `Flow`
* przekazanie danych do ViewModel

**Przepływ danych:**

Firestore → Repository → ViewModel → UI

**Zgodność ze specyfikacją:**

* dodanie/usunięcie posiłku natychmiast aktualizuje UI
* dane są filtrowane po użytkowniku i dacie
* spełnia wymagania dotyczące dashboardu i logu

## 10. Pokrycie funkcjonalności

| Wymaganie      | Implementacja                              |
| -------------- | ------------------------------------------ |
| Authentication | AuthViewModel + FirestoreAuthRepository    |
| Meals          | AddMealViewModel + FirestoreMealRepository |
| Dashboard      | HomeViewModel                              |
| Water          | WaterViewModel                             |
| Shopping       | ShoppingViewModel                          |
| Plans          | PlansViewModel                             |
| Profile        | ProfileViewModel                           |

Każde wymaganie funkcjonalne ma bezpośrednie odzwierciedlenie w implementacji.

## 11. Warstwa widoku (UI)

Aplikacja wykorzystuje Jetpack Compose do budowy interfejsu.

**Główne ekrany:**

* Home
* Log
* Shopping
* Water
* Plans
* Profile

Responsywność:

Dla ekranu `WaterScreen`:

* wykorzystano `fillMaxWidth()` oraz `weight(1f)`
* layout dostosowuje się do różnych szerokości ekranów
* testowalny na:
  * telefonie (np. ~360dp)
  * większym ekranie/tablecie (~600dp+)

Zapewnia poprawne wyświetlanie dla co najmniej dwóch rozmiarów urządzeń.
