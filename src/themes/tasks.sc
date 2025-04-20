theme: /Tasks
    
    state: CreateTask
        if: _.isEmpty($session.task)
            script: $session.task = {};
            a: Напишите название задачи
        else:
            go!: /Tasks/CreateTask/ClarifyTask
        q: * || toState="/Tasks/CreateTask/GetName"

        state: ResetTask
            script: $session.task = {};
            go!: /Tasks/CreateTask/GetName

        state: GetName
            if: _.isEmpty($session.task.description)
                script: $session.task.name = $session.task.name || $request.query;
                a: Напишите описание для задачи
            else:
                go!: /Tasks/CreateTask/GetDescription
            buttons:
                "Нет описания" -> /Tasks/CreateTask/GetDescription

        state: GetDescription
            script: $session.task.description = $request.query;
            a: Укажите дедлайн выполнения задачи
            buttons:
                "Нет дедлайна" -> /Tasks/CreateTask/GetDeadline
            q: * || toState="/Tasks/CreateTask/GetWrongDeadline"
            q: * (нет/без/~бессрочный/~не ~важен) * || toState="/Tasks/CreateTask/GetDeadline"
            q: * @duckling.date::date * || toState="/Tasks/CreateTask/GetDeadline"

        state: GetDeadline
            a: Отлично! Приступаю к созданию задачи
            script:
                if ($parseTree.date?.value) $session.task.deadline = $parseTree.date.value;
                $session.task.status = "TO DO";
                $session.task.createdAt = moment();
                $client.tasks.push($session.task);
            a: Задача создана
            go!: /HowCanIHelpYou

        state: GetWrongDeadline
            a: К сожалению, не удалось распознать дату в вашем ответе. Попробуйте ещё раз
            go!: /Tasks/CreateTask/GetName

        state: ClarifyTask
            if: $session.task.description:
                a: Вы уже заполняли заявку:

                    Название: {{$session.task.name}}
                    Описание: {{$session.task.description}}

                    Хотите продолжить заполнение?
                buttons:
            else:
                a: Вы уже заполняли заявку:

                    Название: {{$session.task.name}}

                    Хотите продолжить заполнение?
                buttons:
                    "Да" -> /Tasks/CreateTask/GetName
                    "Нет" -> /Tasks/CreateTask/ResetTask
    
    state: GetTasks
        script:
            delete $session.buttonsPaginationMessage;
            $session.paginatorCurPos = 0;
        if: _.isEmpty($temp.tasks)
            go!: /NoTasks
        else:
            a: В каком виде вы хотите посмотреть задачи?
            buttons:
                "Сначала новые" -> /Tasks/GetTasks/Search
                "Сначала горящие" -> /Tasks/GetTasks/Search
                "С определённым статусом" -> /Tasks/GetTasks/Search

            state: Search
                script:
                    if ($session.buttonsPaginationMessage) deleteMessage($session.buttonsPaginationMessage);
                    $session.buttonsPaginationMessage = sendMessage("Выберите задачу", pagination($session.buttons, $session.paginatorCurPos, 5));
                
                state: GetNumber
                    q: * @duckling.number *
                    script:
                        $temp.number = parseInt($parseTree.words[0]);
                    if: $temp.number < 1  $temp.number > $Features.length
                        a: Недопустимый индекс. Выберите число от 1 до {{ $Features.length }}
                        go!: /AllSolutions
                    else:
                        script:
                            deleteMessage($session.buttonsPaginationMessage);
                            delete $session.buttonsPaginationMessage;
                            $client.index = $temp.number - 1;
                            $reactions.transition("/Tasks/GetTasks/Search");
                
                state: MoreBack
                    q: * (=>:more) *
                    q: * ($regex<\<\=>:back) *
                    q: * (вперед:more/назад:back) *
                    script:
                        var mod = $parseTree.value === "more" ? 3 : -3;
                        $session.paginatorCurPos += mod;
                        $reactions.transition("/Tasks/GetTasks/Search");

            state: ShowTask
                a: Данные задачи:
                script: $session.task = null;
                buttons:
                    "Обновить" -> /Tasks/UpdateTask
                    "Удалить" -> /Tasks/DeleteTask
                    "Вернуться к списку" -> /Tasks/GetTasks/Search

            state: NoTasks
                a: На текущий момент у вас нет задач. Хотите создать?
                buttons:
                    "Да, создать задачу" -> /Tasks/CreateTask
                    "Нет, вернуться в меню" -> /HowCanIHelpYou

    state: UpdateTask
        a: Что именно вы хотите изменить?
        buttons:
            "Название"
            "Описание"
            "Дедлайн"
            "Статус"

            state: UpdateField
                a: Поле обновлено

    state: DeleteTask
        a: Вы точно хотите удалить задачу?

        state: Confirm
            script: $client.tasks.remove($session.task.id);
            a: Хорошо! Задача {{$session.task.id}} удалена

        state: Cancel
            a: Хорошо, возвращаю тебя к задаче
            go!: /GetTasks