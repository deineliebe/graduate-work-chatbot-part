bind("postProcess", function() {
    $.session.lastState = $.currentState;
    return $.session.lastState;
});

function countRepeatsInRow() {
    $.temp.entryState = $.currentState;
    if ($.session.lastState === $.currentState) {
        $.session.repeatsInRow += 1;
    } else {
       $.session.repeatsInRow = 1;
    }
    return $.session.repeatsInRow;
}

function cancelEvent(eventId) {
        try {
            if (eventId) $pushgate.cancelEvent(eventId);
        } catch (err) {
            log(err);
        }
    }

function createScheduledEvent(interval, eventName, eventData, channelType, botId, chatId) {
    var whatTimeNow = $jsapi.dateForZone("Europe/Moscow", "yyyy-MM-dd HH:mm:ss");
    var scheduledTime = (moment(whatTimeNow).add(interval)).format("YYYY-MM-DDTHH:mm:ss");
    var event = $pushgate.createEvent(
        scheduledTime,
        eventName,
        eventData,
        channelType,
        botId,
        chatId
    );
    return event.id;
}

function getClientUsername() {
    if ($.request.channelType === "chatapi") return $.request.rawRequest.clientId;
    return "test";
}

function pagination(elements, position, numOfButtons) {
    var i;
    var buttons = [];

    if (position > elements.length) {
        position = position - numOfButtons;
    } else if (position < 0) {
        position = 0;
    }

    if (position > 0) {
        buttons.push({text: "Назад"});
    }
    for (i = position; i < Math.min((position + numOfButtons - (position > 0)
        - (position + 2 < elements.length)), elements.length); i++) {
        buttons.push(elements[i]);
    }
    if (i < elements.length) {
        buttons.push({text: "Вперёд"});
    }
    return buttons;
}

function sendMessage(text, _buttons, _removeKeyboard) {
    if (isTelegramChannel()) {
        var body = {
            "chat_id": $.client.chatId,
            "text": text,
            "parse_mode": "markdown",
            "reply_markup": {}
        };
        if (_buttons) body.reply_markup.keyboard = [_buttons];
        if (_removeKeyboard) body.reply_markup.remove_keyboard = true;
        var result = $http.post($.injector.baseTelegramUrl + "bot" + $env.get("TELEGRAM_TOKEN") + "/sendMessage", {
            body: body
        });
        return result && result.data && result.data.result && result.data.result.message_id;
    } else {
        $reactions.answer(text);
        if (_buttons) $reactions.buttons(_buttons);
    }
}

function deleteMessage(message_id) {
    if (!isTelegramChannel()) return;
    $.response.replies = $.response.replies || [];
    $.response.replies.push({
        type: "raw",
        body: {
            "message_id": message_id
        },
        method: "deleteMessage"
    });
}

function generatePassword(len){
    len = len * (-1);
    return Math.random().toString(36).slice(len);
}

function isTelegramChannel() {
    return !testMode() && $.request.channelType === "telegram";
}