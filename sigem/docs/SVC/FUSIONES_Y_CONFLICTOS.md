# Fusiones y conflictos

Antes de fusionar, actualizar la rama base y revisar el diff. Preferir Pull Requests para que quede registro de descripcion, revision y resultado de pruebas.

Si aparece un conflicto:
1. Leer `git status` para identificar archivos.
2. Abrir cada archivo y resolver los marcadores `<<<<<<<`, `=======`, `>>>>>>>` conservando la logica correcta.
3. Ejecutar pruebas y revisar `git diff`.
4. Marcar los archivos resueltos con `git add <archivo>`.
5. Completar la fusion con `git commit` si Git no la completa automaticamente.
6. Subir los cambios y dejar constancia en el PR.

Cancelar una fusion no completada: `git merge --abort`. Usarlo solo cuando se quiera abandonar esa fusion y el estado permita abortarla.
