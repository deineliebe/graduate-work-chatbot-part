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
                    $client.id = pg.users.getUser($client.chatId).id;
                    log("!!! " + toPrettyString(await pg.users.getUser($client.chatId)));
                }
                $client.tasks = [];
            a: Привет! Я - бот помощник для планирования задач
        go!: /HowCanIHelpYou

    state: HowCanIHelpYou
        q!: {* (меню*/~навигация/список (~вариант/~опция)/~выбор)}
        a: Чем я могу тебе помочь?
        buttons:
            "Создай задачу" -> /Tasks/CreateTask
            "Покажи задачи" -> /Tasks/GetTasks
            "Настройки" -> /Settings

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

            state: ConfirmEmail
                q: * @duckling.email * || fromState = "/Settings"
                script:
                    $client.email = $parseTree.value;
                a: Спасибо! E-mail изменён на {{$client.email}}
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

    state: GlobalCatchAll
        event!: noMatch
        a: К сожалению, не смогла понять ваш запрос. Подскажите, что именно вас интересует?
        go!: /HowCanIHelpYou