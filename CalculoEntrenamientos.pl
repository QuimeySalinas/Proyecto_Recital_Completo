member(X, [X|_]).
member(X, [_|T]) :- member(X, T).

subtract([], _, []).
subtract([H|T], R, Result) :- member(H, R), subtract(T, R, Result).
subtract([H|T], R, [H|Result]) :- \+ member(H, R), subtract(T, R, Result).

rol_necesario(voz_principal).
rol_necesario(bajo).
rol_necesario(guitarra_eléctrica).
rol_necesario(batería).
rol_necesario(voz_principal).
rol_necesario(bajo).
rol_necesario(guitarra_eléctrica).
rol_necesario(batería).
rol_necesario(piano).
rol_necesario(voz_principal).
rol_necesario(batería).
rol_necesario(voz_principal).
rol_necesario(bajo).
rol_necesario(guitarra_eléctrica).
rol_necesario(batería).
rol_necesario(voz_principal).
rol_necesario(bajo).
rol_necesario(guitarra_eléctrica).
rol_necesario(voz_principal).
rol_necesario(guitarra_eléctrica).
rol_necesario(armónica).
rol_necesario(batería).
rol_necesario(voz_principal).
rol_necesario(bajo).
rol_necesario(guitarra_eléctrica).
rol_necesario(armónica).
rol_necesario(piano).
rol_necesario(voz_principal).
rol_necesario(batería).
rol_necesario(voz_principal).
rol_necesario(bajo).
rol_necesario(batería).
rol_necesario(voz_principal).
rol_necesario(guitarra_eléctrica).
rol_necesario(batería).
rol_necesario(coros).
rol_necesario(voz_principal).
rol_necesario(bajo).
rol_necesario(guitarra_eléctrica).
rol_necesario(batería).
rol_necesario(piano).
rol_necesario(voz_principal).
rol_necesario(batería).

artista_base(Gustavo_Cerati,voz_principal).
artista_base(Gustavo_Cerati,guitarra_eléctrica).
artista_base(Ricardo_Mollo,voz_principal).
artista_base(Ricardo_Mollo,guitarra_eléctrica).
artista_base(Fito_Páez,piano).
artista_base(Fito_Páez,voz_principal).
artista_base(Adrián_Barilari,voz_principal).


cuantos_entrenamientos(N) :-
    findall(R, rol_necesario(R), Req),
    findall(X, (artista_base(_, X); artista_contratado(_, X)), Roles),
    subtract(Req, Roles, Faltantes),
    length(Faltantes, N).
