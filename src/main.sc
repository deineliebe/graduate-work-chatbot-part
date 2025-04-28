require: slotfilling/slotFilling.sc
  module = sys.zb-common

require: requirements.sc

init:
    $global.CATCH_ALL_LIMIT = $injector.catchAllLimit;

require: scripts/postgres.js
    type = scriptEs6
    name = pg

require: patterns.sc
  module = sys.zb-common


theme: /

    state: Start
        q!: $regex</start>
        if: $client.isAuthorized
            a: Привет! Рада видеть тебя, {{$client.username}}!
        else:
            scriptEs6:
                $client.chatId = $request.data.chatId;
                await pg.users.addUser($request.data.chatId);
                if (testMode()) $.client.id = 0;
                else {
                    $client.id = (await pg.users.getUser($client.chatId)).id;
                }
            a: Привет! Я - бот помощник для планирования задач
        go!: /HowCanIHelpYou

    state: HowCanIHelpYou
        q!: {* (меню*/~навигация/список (~вариант/~опция)/~выбор)}
        a: Чем я могу тебе помочь?
        buttons:
            "Создай задачу" -> /Tasks/CreateTask
            "Покажи задачи" -> /Tasks/GetTasks
            "Настройки" -> /Settings
            "Контакты" -> /Contacts

    state: Settings
        q!: [~показать/~поменять] (~настройки/~параметр/конфиг*)
        q!: (~настроить)
        a: Выберите, что вы хотите сделать
        buttons:
            "Поменять электронный адрес" -> /Settings/ChangeEmail
            "Вернуться в меню" -> /HowCanIHelpYou
        q: * duckling.email * || toState = "/Settings/ChangeEmail/ConfirmEmail"

        state: ChangeEmail
            a: Введите электронный адрес:

            state: GetEmail
                q: * @duckling.email * || fromState = "/Settings"
                script: $session.code = Math.floor(Math.random() * $injector.emailCodeLimit);
                a: Спасибо! На адрес {{$client.email}}  отправлен код. Введи его
                Email:
                    destination = {{$client.email}}
                    subject = Код для регистрации почты (Task Planner)
                    text = {{$session.code}}
                    errorState = /Settings/GetEmail/Error
                buttons:
                    "Назад" -> /Settings/ChangeEmail
                    "Вернуться в меню" -> /HowCanIHelpYou

                state: Confirm
                    q: * @duckling.number *
                    if: $session.code
                        script:
                            $session.password = changeEmail($client.id, $client.email, $injector.passwordLength);
                            changeEmail($client.id, $client.email, $session.password);
                        Email:
                            destination = {{$client.email}}
                            subject = Код для регистрации почты (Task Planner)
                            text = Мы привязали ваш адрес! Пароль: {{$session.password}}
                            errorState = /Settings/GetEmail/Error
                        a: Отлично! Ваш адрес привязан
                        go!: /HowCanIHelpYou
                    else:
                        a: К сожалению, код некорректный. Попробуете ещё раз?
                        buttons:
                            "Вернуться в меню" -> /HowCanIHelpYou

                state: Error
                    a: К сожалению, произошла ошибка. Попробуйте привязать email через сайт
                    go!: /HowCanIHelpYou

            state: CatchAll
                event: noMatch
                a: К сожалению, не смогла распознать электронный адрес. 
                buttons:
                    "Вернуться в меню" -> /HowCanIHelpYou

        state: CatchAll
            event: noMatch
            a: К сожалению, не смогла понять, что вы хотите сделать. Уточните ваш запрос
            buttons:
                "Поменять электронный адрес" -> /Settings/ChangeEmail
                "Вернуться в меню" -> /HowCanIHelpYou

    state: Contacts
        q!: $regex</contacts>
        a: Привет!

            Я Марина, это - мой курсовой проект. С этим чат-ботом связан сайт: app.smalltaskplanner.ru

    state: GlobalCatchAll
        event!: noMatch
        a: К сожалению, не смогла понять ваш запрос. Подскажите, что именно вас интересует?
        go!: /HowCanIHelpYou