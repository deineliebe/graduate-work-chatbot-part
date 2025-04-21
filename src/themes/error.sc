theme: /Error
    state: LimitHandler || noContext = true
        event!: lengthLimit
        event!: timeLimit
        event!: nluSystemLimit
        a: Кажется, что-то пошло не так. Попробуй укоротить свой запрос

    state: ScriptAndDialogError || noContext = true
        a: Кажется, что-то пошло не так