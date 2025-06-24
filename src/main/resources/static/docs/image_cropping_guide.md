# Przewodnik po kadrowaniu obrazów w aplikacji

## System współrzędnych

W aplikacji oraz w usłudze Cloudinary, współrzędne (x, y) są liczone od **lewego górnego rogu** obrazka. Jest to standardowy system współrzędnych używany w większości bibliotek do przetwarzania obrazów i w technologiach webowych.

## Przykładowe zapytanie do ustawienia obrazka jako główny

Aby ustawić obrazek jako główny i jednocześnie go wykadrować, należy wysłać zapytanie PATCH na endpoint:

```
PATCH /api/profile/images/{imageId}/main
```

gdzie `{imageId}` to identyfikator obrazka, który chcesz ustawić jako główny.

Przykładowa treść zapytania (JSON):

```json
{
  "x": 50,
  "y": 30,
  "width": 400,
  "height": 400
}
```

**Ważne:** Wartości `width` i `height` muszą być identyczne (kadrowanie musi być kwadratowe), ponieważ zdjęcie główne wymaga proporcji 1:1. System automatycznie przeskaluje wykadrowany obszar do rozmiaru 512x512 pikseli.

## Limity kadrowania

W obecnej implementacji aplikacji **nie ma jawnego sprawdzania**, czy wartości `x + width` oraz `y + height` nie przekraczają wymiarów oryginalnego obrazka. Oznacza to, że:

1. Użytkownik powinien upewnić się, że wybierany obszar kadrowania mieści się w granicach obrazka.
2. Jeśli wybrane współrzędne wykraczają poza granice obrazka, Cloudinary może:
   - Obciąć obszar kadrowania do granic obrazka
   - Uzupełnić brakujące obszary przezroczystością lub kolorem tła
   - W niektórych przypadkach zwrócić błąd

Dla najlepszych rezultatów zaleca się, aby:
- Wartości `x` i `y` były większe lub równe 0
- Wartości `x + width` i `y + height` nie przekraczały wymiarów oryginalnego obrazka

## Proces kadrowania

Po wysłaniu zapytania:
1. System sprawdza, czy kadrowanie jest kwadratowe (width = height)
2. Obszar określony przez współrzędne (x, y, width, height) jest wycinany z oryginalnego obrazka
3. Wykadrowany obszar jest skalowany do rozmiaru 512x512 pikseli
4. Obrazek jest ustawiany jako główny w profilu użytkownika