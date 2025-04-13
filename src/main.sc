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
        if: $session.isAuthorized
            Привет! Рада видеть тебя, {{$session.username}}!
        else: 
            a: Привет!

                Я - бот помощник для планирования задач.

    state: HowCanIHelpYou
        a: Что ты хочешь сделать сегодня?
        buttons:
            "Создать задачу" -> /CreateTask
            "Ближайшие дедлайны" -> /TheClosestTasks
            "Список задач" -> /GetTasks
    
    state: CreateTask
        a: Напишите название задачи
        q: * || toState="/CreateTask/GetName"

        state: GetName
            script:
                $session.taskName = $request.query
            a: Укажите дедлайн выполнения задачи
            buttons:
                "Нет дедлайна" -> /CreateTask/NoDeadLine
            q: * || toState="/CreateTask/GetWrongDeadline"
            q: * (нет/без/~бессрочный/~не ~важен) * || toState="/CreateTask/GetWrongDeadline"
            q: * @duckling.date::date * || toState="/CreateTask/GetDeadline"

        state: GetDeadline
            a: Отлично! Приступаю к созданию задачи
            a: Задача создана
            go!: /HowCanIHelpYou

        state: GetWrongDeadline
            a: К сожалению, не удалось распознать дату в вашем ответе. Попробуйте ещё раз
            go!: /CreateTask/GetName
    
    state: GetTasks
        script:
            $temp.tasks = [];
        if: !_.isEmpty($temp.tasks)
            a: Вот список ваших задач:

                {{$temp.tasks}}
            go!: /HowCanIHelpYou
        else:
            a: На текущий момент у вас нет задач. Хотите создать?
        buttons:
            "Да, создать задачу" -> /CreateTask
            "Нет, вернуться в меню" -> /HowCanIHelpYou

    state: TheClosestTasks
        script:
            $temp.tasks = [];
        if: !_.isEmpty($temp.tasks)
            a: Вот список ваших задач:

                {{$temp.tasks}}
            go!: /HowCanIHelpYou
        else:
            a: На текущий момент у вас нет задач. Хотите создать?
        buttons:
            "Да, создать задачу" -> /CreateTask
            "Нет, вернуться в меню" -> /HowCanIHelpYou

    state: Hello
        intent!: /привет
        a: Привет! Рада видеть тебя!
        go!: /HowCanIHelpYou

    state: Bye
        intent!: /пока
        a: Пока! Хорошего дня тебя, буду рада увидеть тебя снова.

    state: NoMatch
        event!: noMatch
        a: К сожалению, не смогла понять ваш запрос. Подскажите, что именно вас интересует?
        go!: /HowCanIHelpYou