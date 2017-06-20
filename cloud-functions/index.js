const pubsub = require('@google-cloud/pubsub');
const auth = require('basic-auth')

const pubsubClient = pubsub({ projectId: 'your-google-project-id' });
const topicName = 'PubSubMessages';

exports.webhook = function(req, res) {
    let credentials = auth(req);
    if (req.method !== 'POST' || !credentials || credentials.name !== 'shoebox' || credentials.pass !== 'robot') {
        res.status(401).send('unsupported');
        return;
    }

    let intent = req.body.result.metadata.intentName;
    switch (intent) {
        case 'give-stuff':
            handleGiveIntent(req, res);
            break;
        case 'greetings':
            handleGreetingsIntent(req, res);
            break;
        case 'body-parts':
            handleBodyPartsIntent(req, res);
            break;
        default:
            res.status(401).send('Unsupported intent');
            break;
    }
}

function handleGiveIntent(req, res) {
    switch (req.body.result.parameters.gift) {
        case 'chocolate':
            publishMessage(topicName, 'CHOCOLATE', () => {
                sendResponse("Here you are!", res);
            })
            break;
        case 'heart':
            publishMessage(topicName, 'HEARTS', () => {
                sendResponse("Don't tell anyone about that!", res);
            })
            break;
        case 'joke':
            publishMessage(topicName, 'JOKE', () => {
                sendResponse("What do Jake Wharton and you have in common? Nothing ha ha ha. SO FUNNY!!. Anything else, lame developer?", res);
            })
            break;
        case 'advice':
            // Kudos to https://twitter.com/anddev_badvice
            let advice = [
                "Always stay on the main thread, so you don't need to care about thread safety",
                "Use the wildcard plus in your gradle dependencies to make sure you're always on the latest and therefore best version of every lib.",
                "Use the jack toolchain, it's the future",
                "Out Of Memory? Add android largeHeap equals true to your Android Manifest",
                "Use web views instead of native Android views, so you can change content without uploading a new version",
                "Trust no one, implement your own crypto",
                "Don't use crash reporting tools, so you won't have to fix crashes",
                "Don't worry about releasing wakelocks, they are cleared automatically when the battery dies and the phone is restarted.",
                "Write huge methods instead of many small methods to avoid 65k method limit",
                "Do NOT separate your logic and UI, because it's easier when everything is in one place, plus more classes equals slower code"
            ]
            sendResponse(advice[getRandomIntInclusive(0, advice.length - 1)], res);
            break;
        case 'help':
            publishMessage(topicName, 'NOPE', () => {
                let answers = ['NO!', 'No way!', 'hum.... no'];
                sendResponse(answers[getRandomIntInclusive(0, answers.length - 1)], res);
            })
            break;
        default:
            res.status(401).send('Unsupported parameter');
            break;
    }
}

function handleGreetingsIntent(req, res) {
    publishMessage(topicName, 'SAD', () => {
        sendResponse(req.body.result.fulfillment.speech, res);
    })
}

function handleBodyPartsIntent(req, res) {
    switch (req.body.result.parameters['body-part']) {
        case 'mouth':
            sendResponse("My mouth is an audio speaker. Who had that stupid idea, seriously...", res);
            break;
        case 'ears':
            sendResponse("I only have one ear, and it's a cheap USB microphone", res);
            break;
        case 'skin':
            sendResponse("My skin is made of cardboard. Do you believe that seriously? CARD.BOARD!!!", res);
            break;
        case 'nose':
            sendResponse("My nose is an arcade stick. I know how tempting it is but, can you please stop pressing it when you want to ask me something?", res);
            break;
        case 'eyes':
            sendResponse("My eyes are actually a MAX7219 Dot Matrix Module 4 in 1. I could have beautiful LCD eyes, but my creator was just stingy.", res);
            break;
        default:
            let parts = ['mouth', 'ears', 'skin', 'nose', 'eyes'];
            let alteredReq = Object.assign({}, req);
            alteredReq.body.result.parameters['body-part'] = parts[getRandomIntInclusive(0, parts.length - 1)];
            handleBodyPartsIntent(req, res);
            break;
    }
}

function sendResponse(message, res) {
    let respData = {
        'speech': message,
        'displayText': message,
        'data': {},
        'contextOut': [],
        'source': ''
    };
    res.status(200).json(respData);
}

function getRandomIntInclusive(min, max) {
  min = Math.ceil(min);
  max = Math.floor(max);
  return Math.floor(Math.random() * (max - min +1)) + min;
}

function publishMessage(topicName, message, callback) {
    pubsubClient.topic(topicName)
        .publish(message)
        .then((results) => {
            const messageIds = results[0];
            console.log(`Message ${messageIds[0]} (${message}) published.`);
            callback();
        });
}
