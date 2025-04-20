theme: /Tasks
    
    state: CreateTask
        if: _.isEmpty($session.newTask)
            script: $session.newTask = {};
            a: Напишите название задачи
        else:
            go!: /Tasks/CreateTask/ClarifyTask
        q: * || toState="/Tasks/CreateTask/GetName"

        state: ResetTask
            script: $session.newTask = {};
            go!: /Tasks/CreateTask/GetName

        state: GetName
            if: _.isEmpty($session.newTask.description)
                script: $session.newTask.name = $session.newTask.name || $request.query;
                a: Напишите описание для задачи
            else:
                go!: /Tasks/CreateTask/GetDescription
            buttons:
                "Нет описания" -> /Tasks/CreateTask/GetDescription

        state: GetDescription
            script: $session.newTask.description = $request.query;
            a: Укажите дедлайн выполнения задачи
            buttons:
                "Нет дедлайна" -> /Tasks/CreateTask/GetDeadline
            q: * || toState="/Tasks/CreateTask/GetWrongDeadline"
            q: * (нет/без/~бессрочный/~не ~важен) * || toState="/Tasks/CreateTask/GetDeadline"
            q: * @duckling.date::date * || toState="/Tasks/CreateTask/GetDeadline"

        state: GetDeadline
            a: Отлично! Приступаю к созданию задачи
            script:
                if ($parseTree.date && $parseTree.date.value) $session.newTask.deadline = $parseTree.date.value;
                $session.newTask.status = "TO DO";
                $session.newTask.createdAt = moment();
                $client.tasks.push($session.newTask);
            a: Задача создана
            go!: /HowCanIHelpYou

        state: GetWrongDeadline
            a: К сожалению, не удалось распознать дату в вашем ответе. Попробуйте ещё раз
            go!: /Tasks/CreateTask/GetName

        state: ClarifyTask
            if: $session.newTask.description:
                a: Вы уже заполняли заявку:

                    Название: {{$session.newTask.name}}
                    Описание: {{$session.newTask.description}}

                    Хотите продолжить заполнение?
            else:
                a: Вы уже заполняли заявку:

                    Название: {{$session.newTask.name}}

                    Хотите продолжить заполнение?
            buttons:
                "Да" -> /Tasks/CreateTask/GetName
                "Нет" -> /Tasks/CreateTask/ResetTask

    state: GetTasks
        script:
            $session.paginatorCurPos = 0;
        if: _.isEmpty($client.tasks)
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
                q: * (вперед:more) *
                q: * (назад:back) *
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