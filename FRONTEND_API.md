# MoneyKeeper 闂佸憡鑹惧ù鐑筋敂椤掑嫬绀堢€广儱娴傛导鍌炴煠鏉堛劍顥嗛柣銊у枛瀵剟宕堕妸锝傚亾?
闂佸搫鐗滈崜姘跺几閸愵煈娴栭柨婵嗘噽閸炪劌霉?`codex/test` 闂佸憡甯掑Λ娆撳极椤旇￥浜归柟鎯у暱椤ゅ懎霉閻欏懐鎮奸柣鏍ㄧ矒瀵偆鈧潧鎲￠崐鐐烘煥濞戞ê顨欑紒鍓佹暬瀹曟粌顓奸崶鏈电磽闂佽壈椴稿Λ渚€鎯冮悢鐓幬ュ〒姘ｅ亾闁靛棗顦靛Λ鍐綖椤撶姴鏋犻梺鍛婄墬閻楁捇鍩€椤戞寧顦峰ù婊勫浮瀹曪絾绻濋崒娑樷偓鐢电磽娓氬洤骞楅懚鈺呮煕瑜庨〃鍡樻櫠鐠恒劋娌柡鍥ㄨ壘閳诲繘鏌ｉ～顒€濡搁柍?
## 1. 闂佺硶鏅炲▍锝夈€侀崨顔锯攳闁斥晛鍟╃槐?
- Base URL闂佹寧绋掗悺鐚tp://{host}:8081`
- 闂佽浜介崕杈亹濞戞氨纾奸柣鏂垮椤忛亶鏌涢幘宕囆ょ紒杈ㄥ灴閺佸秴顫?api`
- 闂佺娴氶崜娆戞閹达箑绠抽柕澶堝劚缂嶆捇鏌?  - `POST /api/auth/login`
  - `POST /api/auth/register`
  - `POST /api/auth/google`
  - `GET /swagger-ui.html`
  - `GET /swagger-ui/**`
  - `GET /v3/api-docs/**`
- 闂佺绻戝﹢鍦礊?`/api/**` 婵帗绋掗…鍫ヮ敇婵犳碍鐒惧ù鐘差儐娴犳﹢鎮烽弴姘辩瓘缂?
```http
Authorization: Bearer <token>
```

## 2. 闂佸搫顦崯鏉戭瀶閸濆嫀鐔煎灳瀹曞洠鍋?
閻熸粎澧楅幐鍛婃櫠閻橀潧瀵查柤濮愬€楅崺鐘绘煙缁嬫妲烽悶姘充含閻氶箖寮拌箛锝嗗仴闂佽壈顫夎ぐ鍐囬埡鍛仩闁糕槅鍘剧粣?
- `user`闂佹寧绋掔喊宥呪枍濮椻偓閺屽懎顫濇潏銊︽闂佽娼欓崵鏍濠靛鐭楁い蹇撴搐閸樻挳鎮规担鍛婂仴婵☆偄鐖奸幊娑㈩敂閸曨厾鐣抽梺姹囧妼鐎氼噣寮妶澶婄闁哄鍋€閸嬫挻鎷呯粙璺ㄢ偓鑽ょ磼椤愶絿婀介柍褜鍏涘ù鍥敊閸モ晜瀚婚柨鏃囥€€閸嬫挻绗熼埀顒勫焵椤掍焦鐨戦柣鎿勭節瀹曨亞浠﹂悙顒佹婵炲濮甸…鍥ㄦ叏閹间礁绠戝ù锝堟閹煎ジ鏌℃担鍝勵暭鐎?- `admin`闂佹寧绋掑銊╊敇閹间焦鍋犻柛鈩冾殕閸犲懘鏌ㄥ☉妯垮鐟滄澘寮剁粋鎺楀Ψ閵婏妇銆婇梺娲绘娇閸斿秹宕哄☉姘闁秆勵殕閿涙牠鏌℃担鍝勵暭鐎规挷绶氶弫宥囦沪閸婄喎鐝梺鍛婄懐閸垱绻涢崶顒佸仺闁靛鍎抽悾顓㈡煙缂併垹寮柍褜鍓氬銊╂偂閿熺姴违濞戞挾纰丒 缂備胶濯寸槐鏇㈠箖婵犲洤违濞戞搩娉ka 闁荤姴顑呴崯鎶芥儊椤栫偛违濞撴埃鍋撴繛澶涚畵楠炲骞囬澶癸箓鏌熼璺ㄥ妽闁绘搫绻濋幆鍥┾偓锝庡亜婢跺秹鏌?
闁荤偞绋忛崕閬嶅储閺嶎偅瀚氶悗娑櫳戦～鏍煥?
- JWT 婵炴垶鎼╅崢鑲╃礊鐎ｎ喖绀堢€广儱鎳庨惁鍫曟煕濮樼厧浜剧紒杈╂珬userId`闂侀潧妫斿姊猻erPin`闂侀潧妫斿姊猻ername`闂侀潧妫斿姊le`
- 闂佸搫鎷嬮崳锝夊焵椤掍焦鐨戦柡浣靛€濋獮瀣憥閸屾粎鎲归梺鐓庣枃濡嫰宕埀顒勬⒑椤愶絼浜㈢紒璇插暞鐎靛ジ顢旈崼姘壕?`userId` 闁荤姳绀佸鈥澄涢懜鐢殿浄闁哄稄闄勯惇浠嬫煛娴ｅ搫顣肩€?- 闂佺儵鏅╅崰鏍ㄦ櫠?Kafka闂侀潧妫旂粭鎶sticsearch闂侀潧妫旂挧濯媦ment闂侀潧妫旂粭宄禸bo闂侀潧妫旈懙鎭哻os 閻庤鐡曠亸娆戝垝閿熺姴瀚夊璺烘閸嬫挸顭ㄩ崟鈧笟鈧畷绋课旈崘銊愶箓鏌熼鑳唹闁逞屽墯缁诲倿骞忔导鏉戠伋婵犲﹤瀚埛鏍倶閻愬瓨绀嬫い搴℃喘瀵悂宕瑰☉鎺戜壕婵犲灚鎸剧粈澶娒归敐鍛棛缂佹顦靛浼搭敍閻愭彃骞嬮悗瑙勭摃鐏忔瑧鍒掗敓鐘茬鐎瑰嫰鍋婂Σ鐢告倵閻熺増婀伴柡鍡稻缁嬪顫濋鈧～銈夋倵閸︻厼浠ф?
## 3. 闂佸憡绻傜粔瀵歌姳閺屻儱鍐€闁绘挸娴风涵鈧?
### 3.1 `MkApiResponse<T>`

婵炴垶鎸诲Σ鎺椼€呴敃鍌涘仺闁靛鍊楅懝楣冩煥?
- `/api/auth/**`
- `/api/kafka/**`
- `/api/payments/**`

缂備讲鍋撻弶鐐村娴兼劙鏌?
```json
{
  "code": 200,
  "message": "Operation succeeded",
  "data": {},
  "timestamp": "2026-03-08T12:00:00",
  "requestId": "REQ-1741435200000"
}
```

闁诲孩绋掗〃鍡涱敊瀹€鈧幏鐘碘偓娑櫳戦～鏍煥?
- `code`闂佹寧绋掗惌顔剧箔閻旂厧绀夐柨娑樺閸?- `message`闂佹寧绋掗惌顔剧箔閻旂厧绀夐柍鈺佸暟鍟搁梺?- `data`闂佹寧绋掓穱铏规崲閹达箑鐐婇柣鎰摠濞堝爼鏌?- `timestamp`闂佹寧绋掗懝楣冨箹闁垮鍎熼柡鍐ｅ亾婵＄偛鍊垮?- `requestId`闂佹寧绋掓穱娲敋椤掆偓鏁堥柛灞剧〒閸ㄥジ鎮?
濠电偛顦崝宥夊礈娴煎瓨鏅?
- 闁哄鏅滈悷褏鎮锕€绠抽柕澶堝劚缂嶆捇鏌涘Δ鈧崢鏍ㄧ箾閸ャ劌绶為弶鍫亯琚濋梺鎸庣☉閻℃笨TP 闂佺粯顭堥崺鏍焵椤戣儻鍏岄柣鏍ㄧ矒閺屽懎顫濋鍌氱厬婵炲濮寸粔鍫曞礉瑜斿?`200`
- 闂佸憡鎸哥粔鍫曨敂椤掑啰鐤€闁告稒鐣埀顒€绻戠€电厧螣閸濆嫬寮楅梺鍛婂竾閸婃牠寮?`code`

闁汇埄鍨遍悺鏇綖閸℃鈻旀慨姗嗗墮椤倝鏌ｉ鑽ょ瓘缂?
- `200`闂佹寧绋掔喊宥夊垂濮樿泛绀?- `400`闂佹寧绋掗懝鎯ь嚕椤掑嫬鏋佸ù鐓庣摠閺呪晠鎮?- `401`闂佹寧绋掓穱娲敇閼姐倖瀚氬ù锝嗘偠娴滃ジ鎮?- `403`闂佹寧绋掔喊宥咁焽閸儲鈷旈柟閭︿簽閻熸繈鎮?- `404`闂佹寧绋掓穱铏圭矈椤愨懇鏀﹂柟閭︿簽閻熸繈鎮楀☉娅亜锕?- `409`闂佹寧绋掗懝楣冨疮鐠恒劎鐜?- `501`闂佹寧绋掔喊宥堝暞闂佺鍕垫當闁告垟鈧剚鍤曢柍褜鍓熷畷銉╊敍濞嗘垹蓱闁诲繐绻戠喊宥咃耿椤撶姭鍋撻崷顓炰户妤犵偛娲ㄩ埀顒傛嚀閺堫剟寮抽敐鍡欌枖婵﹩鍓欓～?- `503`闂佹寧绋掔喊宥堝暞闂佺鍕垫畽妞ゎ偓绠撳浼村箚瑜忕涵鈧梺绋跨箲婵炲﹤螞?- `500`闂佹寧绋掔喊宥咃耿閸ヮ剙绀夐柨娑樺娴煎倻鈧鍠栭崐鎼佹偉?
### 3.2 `ApiErrorResponse`

婵炴垶鎸诲Σ鎺椼€呴敃鍌涘仺闁靛鍊楅懝楣冩煥?
- `/api/users/**`
- `/api/categories/**`
- `/api/records/**`
- `/api/notifications/**`
- `/api/notifications/manage/**`
- `/api/excel/**`
- `/api/integrations/**`

缂備讲鍋撻弶鐐村娴兼劙鏌?
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Amount must be greater than zero",
  "path": "/api/records",
  "timestamp": "2026-03-08T12:00:00"
}
```

闁诲孩绋掗〃鍡涱敊瀹€鈧幏鐘碘偓娑櫳戦～鏍煥?
- `status`闂佹寧绋掗鏈P 闂佺粯顭堥崺鏍焵椤戣儻鍏岄柣?- `error`闂佹寧绋掗鏈P 闂佸搫鍊稿ú锕€锕?- `message`闂佹寧绋掑畝鎼佸极婵犲嫭瀚氭い鏍ㄧ〒鍟搁梺?- `path`闂佹寧绋掓穱娲敋椤掆偓鏁堥柛灞剧箘閻斿懐鈧?- `timestamp`闂佹寧绋掑畝鎼佸极婵犲嫭瀚氭い鏍ㄧ⊕椤ρ囨⒒?
濠电偛顦崝宥夊礈娴煎瓨鏅?
- JWT 闂佺懓鍤栭梽鍕春閸涙潙闂柕濞у嫮鍑介梺瑙勪航閸庤尙鈧哎鍊栫粙澶屸偓锝庡亝闂勫秹鏌?`401` 婵炲濮寸粔纾嬨亹閺屻儲鍤勯柤鎭掑劜绗戦梺鍝勭Т濞层倝宕?HTTP 闂備焦瀵ч悷銊╊敋閵堝鏅悘鐐跺亹閻熸繂鈽夐幘顖氫壕闁诲氦顫夌喊宥呂ｉ崨濠冧氦婵炲棗閰ｉ崵?JSON

## 4. 闂佽桨鑳舵晶妤€鐣垫担铏圭＜闁规儳顕埀?
### 4.1 LoginRequest

```json
{
  "username": "alice",
  "password": "123456"
}
```

### 4.2 LoginResponse

```json
{
  "userId": 1,
  "userPin": "Ab12Cd34Ef",
  "username": "alice",
  "token": "xxxxx"
}
```

### 4.3 RegisterRequest

```json
{
  "username": "alice",
  "password": "123456",
  "email": "alice@example.com",
  "firstName": "Alice",
  "lastName": "Smith",
  "phoneNumber": "13800138000"
}
```

### 4.4 GoogleAuthRequest

```json
{
  "idToken": "google-id-token"
}
```

### 4.5 UserCreateRequest

```json
{
  "username": "alice",
  "password": "123456",
  "email": "alice@example.com",
  "firstName": "Alice",
  "lastName": "Smith",
  "phoneNumber": "13800138000",
  "role": "user"
}
```

### 4.6 UserUpdateRequest

```json
{
  "username": "alice_new",
  "password": "newPassword123",
  "email": "alice_new@example.com",
  "firstName": "Alice",
  "lastName": "Smith",
  "phoneNumber": "13800138000",
  "role": "admin"
}
```

### 4.7 CategoryRequest

```json
{
  "name": "婵＄偘鐒﹂崝娆撳Χ?,
  "icon": "utensils",
  "color": "#FF6B6B",
  "type": "闂佽　鍋撴い鏍ㄨ壘濮?
}
```

### 4.8 MoneyKeeperCreateRequest

```json
{
  "userId": 1,
  "categoryId": 2,
  "type": "闂佽　鍋撴い鏍ㄨ壘濮?,
  "amount": 88.50,
  "transactionDate": "2026-03-08",
  "notes": "闂佸憡顨呴悧鎾凰?
}
```

### 4.9 MoneyKeeperUpdateRequest

```json
{
  "categoryId": 2,
  "type": "闂佽　鍋撴い鏍ㄨ壘濮?,
  "amount": 99.00,
  "transactionDate": "2026-03-09",
  "notes": "闂佸搫鎳忓畝鎼佀?
}
```

### 4.10 PaymentIntentRequest

```json
{
  "amount": 1200,
  "currency": "usd"
}
```

闁荤姴娲ら悺銊ノｉ幋锔芥櫖?
- `amount` 闂佸憡顨嗗ú鎴犵礊閸涘瓨鍋ㄩ柛妤冨仦閸婄數绱撴笟鍥у箺闁轰緡鍠楃粋鎺懳熼搹瑙勬緰闂佺粯绮嶅姗€宕伴崨顖楀亾鐟欏嫮鍟茬紒杈ㄧ箚閵囨劙骞橀崘宸瀫婵炲濮撮幊鎾汇€呴敃鈧晥闁稿本鍝庢禍锝吤?0
- `currency` 闂佸憡鐟崹顖滅箔婢跺顕遍柣娆忔噽缁€澶娾槈閹惧磭小缂佸崬鐖煎顕€宕奸弴鐘茬闁荤姳闄嶉崐鎾寸箾閸ヮ剚鍋?`app.payment.default-currency`

### 4.11 User

```json
{
  "id": 1,
  "userPin": "Ab12Cd34Ef",
  "username": "alice",
  "email": "alice@example.com",
  "firstName": "Alice",
  "lastName": "Smith",
  "phoneNumber": "13800138000",
  "role": "user",
  "createdAt": "2026-03-08T12:00:00",
  "updatedAt": "2026-03-08T12:00:00",
  "deletedAt": 0,
  "deletedTime": null,
  "registrationCompletedAt": "2026-03-08T12:00:00"
}
```

闁荤姴娲ら悺銊ノｉ幋锔芥櫖?
- `password` 闂佺粯绮嶅妯猴耿椤忓牆鍙?write-only闂佹寧绋戞總鏃傜箔婢跺顕辨慨姗嗗墮濮ｅ鏌ｅ鍕凡婵犫偓椤忓牆浼犵€广儱鎳愮€瑰鈽?
### 4.12 Category

```json
{
  "id": 1,
  "name": "婵＄偘鐒﹂崝娆撳Χ?,
  "icon": "utensils",
  "color": "#FF6B6B",
  "type": "闂佽　鍋撴い鏍ㄨ壘濮?,
  "userId": 1,
  "createdAt": "2026-03-08T12:00:00",
  "updatedAt": "2026-03-08T12:00:00",
  "deletedAt": 0,
  "deletedTime": null
}
```

### 4.13 MoneyKeeper

```json
{
  "id": 1,
  "userId": 1,
  "categoryId": 2,
  "type": "闂佽　鍋撴い鏍ㄨ壘濮?,
  "amount": 88.50,
  "transactionDate": "2026-03-08",
  "notes": "闂佸憡顨呴悧鎾凰?,
  "createdAt": "2026-03-08T12:00:00",
  "updatedAt": "2026-03-08T12:00:00",
  "deletedAt": 0,
  "deletedTime": null
}
```

### 4.14 MoneyKeeperDTO

```json
{
  "id": 1,
  "userId": 1,
  "categoryId": 2,
  "categoryName": "婵＄偘鐒﹂崝娆撳Χ?,
  "type": "闂佽　鍋撴い鏍ㄨ壘濮?,
  "amount": 88.50,
  "transactionDate": "2026-03-08",
  "updatedAt": "2026-03-08T12:00:00",
  "notes": "闂佸憡顨呴悧鎾凰?
}
```

### 4.15 RecordSummary

```json
{
  "totalIncome": 1000.00,
  "totalExpense": 300.00,
  "balance": 700.00
}
```

### 4.16 NotificationMessage

```json
{
  "title": "Budget alert",
  "message": "Spending is high this month",
  "type": "warning",
  "timestamp": 1741435200000
}
```

闁荤姴娲ら悺銊ノｉ幋锔芥櫖?
- `type` 闂佽　鍋撴い鏍ㄧ☉閻︻噣鎮樿箛鎾剁闁哥喐鎸鹃埀顒佺⊕椤ㄥ牓顢栨担鍦枖閻犲泧鍛崶`success`闂侀潧妫斿姊琣rning`闂侀潧妫斿姊歯fo`闂侀潧妫斿姊時ror`闂侀潧妫斿姊檈artbeat`闂侀潧妫斿姊抩nnect`

### 4.17 KafkaMessageRecord

```json
{
  "topic": "quickstart-events",
  "key": "message",
  "value": "hello",
  "receivedAt": "2026-03-08T12:00:00"
}
```

### 4.18 IntegrationModuleStatusDTO

```json
{
  "module": "kafka",
  "enabled": false,
  "ready": false,
  "implemented": true,
  "summary": "Kafka module is disabled via app.kafka.enabled",
  "metadata": {
    "producerReady": false,
    "consumerReady": false,
    "consumedCount": 0
  }
}
```

## 5. 闁荤姳闄嶉崐娑㈡儊婢舵劕绠抽柕澶堝劚缂?
### 5.1 POST `/api/auth/login`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗墯閸?- 闁荤姴娲弨閬嶆儑閻楀牊濯撮柟鍨缁愭LoginRequest`
- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃鐡塳ApiResponse<LoginResponse>`

### 5.2 POST `/api/auth/logout`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- Header闂佹寧绋掗悺鐙漸thorization: Bearer <token>`
- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃鐡塳ApiResponse<Boolean>`

### 5.3 POST `/api/auth/register`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗墯閸?- 闁荤姴娲弨閬嶆儑閻楀牊濯撮柟鍨缁愭RegisterRequest`
- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃鐡塳ApiResponse<User>`

### 5.4 POST `/api/auth/google`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗墯閸?- 闁荤姴娲弨閬嶆儑閻楀牊濯撮柟鍨缁愭GoogleAuthRequest`
- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃鐡塳ApiResponse<LoginResponse>`

## 6. 闂佹椿娼块崝宥夊春濞戙垹绠抽柕澶堝劚缂?
### 6.1 POST `/api/users`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗亞閻?`admin`
- 闁荤姴娲弨閬嶆儑閻楀牊濯撮柟鍨缁愭UserCreateRequest`
- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃鐡梥er`
- 婵犮垺鍎肩划鍓ф喆閿曞倹鏅慨婵囶儖piErrorResponse`

### 6.2 GET `/api/users/{id}`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗幗閹峰崬霉濠婂懎鍔嬮柛?`admin`
- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃鐡梥er`
- 婵犮垺鍎肩划鍓ф喆閿曞倹鏅慨婵囶儖piErrorResponse`

### 6.3 GET `/api/users/username/{username}`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗幗閹峰崬霉濠婂懎鍔嬮柛?`admin`
- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃鐡梥er`
- 婵犮垺鍎肩划鍓ф喆閿曞倹鏅慨婵囶儖piErrorResponse`

### 6.4 PUT `/api/users/{id}`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗幗閹峰崬霉濠婂懎鍔嬮柛?`admin`
- 闁荤姴娲弨閬嶆儑閻楀牊濯撮柟鍨缁愭UserUpdateRequest`
- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃鐡梥er`
- 婵犮垺鍎肩划鍓ф喆閿曞倹鏅慨婵囶儖piErrorResponse`

### 6.5 DELETE `/api/users/{id}`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗幗閹峰崬霉濠婂懎鍔嬮柛?`admin`
- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵ɑ澧庨弫?body
- 婵犮垺鍎肩划鍓ф喆閿曞倹鏅慨婵囶儖piErrorResponse`

## 7. 闂佸憡甯掑Λ娑氭偖椤愶箑绠抽柕澶堝劚缂?
### 7.1 POST `/api/categories/{id}`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗幗閹峰崬霉濠婂懎鍔嬮柛?`admin`
- Path 闂佸憡鐟ラ崐褰掑汲閻斿吋鏅慨婵囶劉d` 闁诲骸婀遍崑銈咁瀶椤栨粍鍋橀柕濞т椒绮?`userId`
- 闁荤姴娲弨閬嶆儑閻楀牊濯撮柟鍨缁愭CategoryRequest`
- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃顑巃tegory`
- 婵犮垺鍎肩划鍓ф喆閿曞倹鏅慨婵囶儖piErrorResponse`

### 7.2 GET `/api/categories/{id}`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗墮閻庤崵绱掗銈囧笡濠⒀冪Ф娴狅箓鎮欓崹顐ｆ闂佽鎸抽弨閬嶅垂?`admin`
- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃顑巃tegory`
- 婵犮垺鍎肩划鍓ф喆閿曞倹鏅慨婵囶儖piErrorResponse`

### 7.3 GET `/api/categories/user/{userId}`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗幗閹峰崬霉濠婂懎鍔嬮柛?`admin`
- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃顑巃tegory[]`
- 婵犮垺鍎肩划鍓ф喆閿曞倹鏅慨婵囶儖piErrorResponse`

### 7.4 GET `/api/categories/type/{type}`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗幗閻濐噣姊洪锝嗙殤闁轰降鍊濋獮瀣槹鎼淬垻鐣介梺鐓庮殠娴滄粎鎹㈡笟鈧幆鍐礋閸撲胶顦紓浣哄缁辨洟骞冩繝鍥у窛婵☆垰鎼拋鏌ユ煟椤忓嫷鍎忛柛娆忕箻閺?- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃顑巃tegory[]`
- 婵犮垺鍎肩划鍓ф喆閿曞倹鏅慨婵囶儖piErrorResponse`

### 7.5 PUT `/api/categories/{id}`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗墮閻庤崵绱掗銈囧笡濠⒀冪Ф娴狅箓鎮欓崹顐ｆ闂佽鎸抽弨閬嶅垂?`admin`
- 闁荤姴娲弨閬嶆儑閻楀牊濯撮柟鍨缁愭CategoryRequest`
- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃顑巃tegory`
- 婵犮垺鍎肩划鍓ф喆閿曞倹鏅慨婵囶儖piErrorResponse`

### 7.6 DELETE `/api/categories/{id}`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗墮閻庤崵绱掗銈囧笡濠⒀冪Ф娴狅箓鎮欓崹顐ｆ闂佽鎸抽弨閬嶅垂?`admin`
- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵ɑ澧庨弫?body
- 婵犮垺鍎肩划鍓ф喆閿曞倹鏅慨婵囶儖piErrorResponse`

### 7.7 GET `/api/categories/list`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗幗閻濐噣姊洪锝嗙殤闁轰降鍊濋獮瀣偪椤栨碍顔囬悗瑙勭摃娴滎剙鈻撻幋锕€绀傞柕濞炬櫅閸斻儵鏌涢幒鎴烆棤閻炴凹鍋婇弫宥呯暆閳ь剟顢橀幖浣瑰仩闁糕剝顨嗛崰鍛存煕韫囧濮傞柛搴㈡尦瀹曟岸宕卞▎鎺濇蕉
- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃顑巃tegory[]`

### 7.8 GET `/api/categories/user/{userId}/type/{type}`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗幗閹峰崬霉濠婂懎鍔嬮柛?`admin`
- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃顑巃tegory[]`
- 婵犮垺鍎肩划鍓ф喆閿曞倹鏅慨婵囶儖piErrorResponse`

### 7.9 GET `/api/categories/list/{type}`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗幗閻濐噣姊洪锝嗙殤闁轰降鍊濋獮瀣煥鐎ｎ偂绮繝銏ｅ煐閼规崘銇愯閹虫鎮剧仦鍓х暯闂佺厧顨庢禍婊呮崲娓氣偓閹啴宕熼崜浣侯槷缂備胶濯寸槐鏇㈠箖婵犲洤宸濇俊顖氭惈鐠佹煡鏌?`userId` 闁哄鏅涘ú锕傚箮?- Query 闂佸憡鐟ラ崐褰掑汲閻斿吋鏅慨婵囶劘serId` 闂佸憡鐟崹鍫曞焵?- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃顑巃tegory[]`
- 婵犮垺鍎肩划鍓ф喆閿曞倹鏅慨婵囶儖piErrorResponse`

## 8. 闁荤姳鐒︽刊鑺ュ緞閸曨厽濯奸柡澶庢硶缁夊潡鏌熼幁鎺戝姎鐟?
### 8.1 POST `/api/records`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗幗閻濐噣姊洪锝嗙殤闁轰降鍊濋獮瀣暋閺夋寧閿梺鐓庣－閺咁偆鍒掓导瀛樺殜妞ゅ繐瀚粻鎺楁煕閹烘挾鈽夌紓鍌涙崌閺佸秴鐣濋埀顒勵敇閹间焦鍋犻柛鈩冾殕閸犲懘鏌涘▎妯虹仧閻犳劗鍠愮粋鎺旂磼濡搫顥嶉梺娲绘娇閸斿秹宕哄☉銏犵婵炴垶顭囩槐?- 闁荤姴娲弨閬嶆儑閻楀牊濯撮柟鍨缁愭MoneyKeeperCreateRequest`
- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃鐡塷neyKeeper`
- 婵犮垺鍎肩划鍓ф喆閿曞倹鏅慨婵囶儖piErrorResponse`

### 8.2 GET `/api/records/{id}`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨妯诲墯閸炲墎鎲搁悧鍫熺濠⒀冪Ф娴狅箓鎮欓崹顐ｆ闂佽鎸抽弨閬嶅垂?`admin`
- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃鐡塷neyKeeper`
- 婵犮垺鍎肩划鍓ф喆閿曞倹鏅慨婵囶儖piErrorResponse`

### 8.3 GET `/api/records/user/{userId}`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗幗閹峰崬霉濠婂懎鍔嬮柛?`admin`
- Query 闂佸憡鐟ラ崐褰掑汲閻斿吋鏅慨婵囶劖tartDate`闂侀潧妫斿姊昻dDate` 闂婎偄娲ら幊搴ㄦ晲?- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃鐡塷neyKeeper[]`
- 婵犮垺鍎肩划鍓ф喆閿曞倹鏅慨婵囶儖piErrorResponse`

### 8.4 GET `/api/records/user/{userId}/type/{type}`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗幗閹峰崬霉濠婂懎鍔嬮柛?`admin`
- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃鐡塷neyKeeper[]`
- 婵犮垺鍎肩划鍓ф喆閿曞倹鏅慨婵囶儖piErrorResponse`

### 8.5 PUT `/api/records/{id}`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨妯诲墯閸炲墎鎲搁悧鍫熺濠⒀冪Ф娴狅箓鎮欓崹顐ｆ闂佽鎸抽弨閬嶅垂?`admin`
- 闁荤姴娲弨閬嶆儑閻楀牊濯撮柟鍨缁愭MoneyKeeperUpdateRequest`
- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃鐡塷neyKeeper`
- 婵犮垺鍎肩划鍓ф喆閿曞倹鏅慨婵囶儖piErrorResponse`

### 8.6 DELETE `/api/records/{id}`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨妯诲墯閸炲墎鎲搁悧鍫熺濠⒀冪Ф娴狅箓鎮欓崹顐ｆ闂佽鎸抽弨閬嶅垂?`admin`
- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵ɑ澧庨弫?body
- 婵犮垺鍎肩划鍓ф喆閿曞倹鏅慨婵囶儖piErrorResponse`

### 8.7 GET `/api/records/list`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗幗閻濐噣姊洪锝嗙殤闁轰降鍊濋獮瀣偪椤栨碍顔囬悗瑙勭摃娴滎剙鈻撻幋锕€绀傞柕濞炬櫅閸斻儵鎮规担瑙勭凡缂傚秴绉归弫宥呯暆閳ь剟顢橀幖浣瑰仩闁糕剝顨嗛崰鍛存煕韫囧濮傞柛搴☆嚟閹峰寮剁捄銊?- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃鐡塷neyKeeper[]`

### 8.8 GET `/api/records/listWithCategoryName/{userId}`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗幗閹峰崬霉濠婂懎鍔嬮柛?`admin`
- Query 闂佸憡鐟ラ崐褰掑汲閻斿吋鏅慨婵囶劖tartDate`闂侀潧妫斿姊昻dDate` 闂佸憡鐟崹鍫曞焵?- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃鐡塷neyKeeperDTO[]`
- 婵犮垺鍎肩划鍓ф喆閿曞倹鏅慨婵囶儖piErrorResponse`

### 8.9 GET `/api/records/listByCategoryName/{categoryName}/{userId}`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗幗閹峰崬霉濠婂懎鍔嬮柛?`admin`
- Query 闂佸憡鐟ラ崐褰掑汲閻斿吋鏅慨婵囶劖tartDate`闂侀潧妫斿姊昻dDate` 闂佸憡鐟崹鍫曞焵?- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃鐡塷neyKeeperDTO[]`
- 婵犮垺鍎肩划鍓ф喆閿曞倹鏅慨婵囶儖piErrorResponse`

### 8.10 GET `/api/records/list/{userId}`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗幗閹峰崬霉濠婂懎鍔嬮柛?`admin`
- Query 闂佸憡鐟ラ崐褰掑汲閻斿吋鏅慨婵囶劗ype`闂侀潧妫斿姊artDate`闂侀潧妫斿姊昻dDate` 闂佸憡鐟崹鍫曞焵?- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃鐡塷neyKeeper[]`
- 婵犮垺鍎肩划鍓ф喆閿曞倹鏅慨婵囶儖piErrorResponse`

### 8.11 GET `/api/records/summary/{userId}`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗幗閹峰崬霉濠婂懎鍔嬮柛?`admin`
- Query 闂佸憡鐟ラ崐褰掑汲閻斿吋鏅慨婵囶劖tartDate`闂侀潧妫斿姊昻dDate` 闂佸憡鐟崹鍫曞焵?- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃鐡揺cordSummary`
- 婵犮垺鍎肩划鍓ф喆閿曞倹鏅慨婵囶儖piErrorResponse`

## 9. Excel 闁诲海鏁搁崢褔宕甸銏犵闁靛鍎辩紞?
### 9.1 GET `/api/excel/download/{userId}`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗幗閹峰崬霉濠婂懎鍔嬮柛?`admin`
- Query 闂佸憡鐟ラ崐褰掑汲閻斿吋鏅?  - `type`闂佹寧绋掗懝鎹愩亹閺屻儲鐒?  - `startDate`闂佹寧绋掗懝鎹愩亹閺屻儲鐒诲鑸电〒缁€濉yyy-MM-dd`
  - `endDate`闂佹寧绋掗懝鎹愩亹閺屻儲鐒诲鑸电〒缁€濉yyy-MM-dd`
- 闂佺懓鐡ㄩ崝鏇熸叏濞戞瑦浜ら柡鍌涘缁€鈧梺鎸庣⊕椤戞伇cel 闂佸搫鍊稿ú锝呪枎閵忕妴?- 婵犮垺鍎肩划鍓ф喆閿旇姤浜ら柡鍌涘缁€鈧梺鎸庣⊕閻＄嫕piErrorResponse`

闁荤姴娲ら悺銊ノｉ幋锔芥櫖?
- 闂佸搫鍊稿ú锝呪枎閵忋倕瑙︾€广儱鎳愮粔濂告煕閹惧磭小閻?`records_yyyy-MM-dd.xlsx`
- 闂侀潻璐熼崝搴★耿閳ユ剚娼伴柨婵嗘噽绾捐鈹戦纰卞剰闁哥喐鎸搁湁濞达綁顥撻娲煕閹惧磭鈻岀紒杈ㄧ箞瀵爼宕橀绛嬧偓娆撴煕濠婂啯婀版俊顐熸櫊瀵敻鎮㈢拠鑼Ш闂佹悶鍎崇换婵堟娴兼潙绀傞柛顐ゅ枔婢р€澄?
## 10. 闂備緡鍋呭銊╂偂閿熺姴绠抽柕澶堝劚缂?
### 10.1 GET `/api/notifications/subscribe/{userId}`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗幗閹峰崬霉濠婂懎鍔嬮柛?`admin`
- 闂佺懓鐡ㄩ崝鏇熸叏濞戞瑦浜ら柡鍌涘缁€鈧梺鎸庣⊕閻＄尲ext/event-stream`
- 婵犮垺鍎肩划鍓ф喆閿旇姤浜ら柡鍌涘缁€鈧梺鎸庣⊕閻?01` 闂?`403`

### 10.2 POST `/api/notifications/send/{userId}`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗幗閹峰崬霉濠婂懎鍔嬮柛?`admin`
- 闁荤姴娲弨閬嶆儑閻楀牊濯撮柟鍨缁愭NotificationMessage`
- 闂佺懓鐡ㄩ崝鏇熸叏濞戞瑦浜ら柡鍌涘缁€鈧梺鎸庣⊕濮樸劑宕欐禒瀣闁搞儯鍔嶉幏?`Message sent`
- 婵犮垺鍎肩划鍓ф喆閿旇姤浜ら柡鍌涘缁€鈧梺鎸庣⊕閻＄嫕piErrorResponse`

### 10.3 POST `/api/notifications/broadcast`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗亞閻?`admin`
- 闁荤姴娲弨閬嶆儑閻楀牊濯撮柟鍨缁愭NotificationMessage`
- 闂佺懓鐡ㄩ崝鏇熸叏濞戞瑦浜ら柡鍌涘缁€鈧梺鎸庣⊕濮樸劑宕欐禒瀣闁搞儯鍔嶉幏?`Broadcast sent`
- 婵犮垺鍎肩划鍓ф喆閿旇姤浜ら柡鍌涘缁€鈧梺鎸庣⊕閻＄嫕piErrorResponse`

### 10.4 POST `/api/notifications/send/{userId}/success`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗幗閹峰崬霉濠婂懎鍔嬮柛?`admin`
- Query 闂佸憡鐟ラ崐褰掑汲閻斿吋鏅慨婵囶劗itle`闂侀潧妫斿姊瀍ssage`
- 闂佺懓鐡ㄩ崝鏇熸叏濞戞瑦浜ら柡鍌涘缁€鈧梺鎸庣⊕濮樸劑宕欐禒瀣闁搞儯鍔嶉幏?- 婵犮垺鍎肩划鍓ф喆閿旇姤浜ら柡鍌涘缁€鈧梺鎸庣⊕閻＄嫕piErrorResponse`

### 10.5 POST `/api/notifications/send/{userId}/error`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗幗閹峰崬霉濠婂懎鍔嬮柛?`admin`
- Query 闂佸憡鐟ラ崐褰掑汲閻斿吋鏅慨婵囶劗itle`闂侀潧妫斿姊瀍ssage`
- 闂佺懓鐡ㄩ崝鏇熸叏濞戞瑦浜ら柡鍌涘缁€鈧梺鎸庣⊕濮樸劑宕欐禒瀣闁搞儯鍔嶉幏?- 婵犮垺鍎肩划鍓ф喆閿旇姤浜ら柡鍌涘缁€鈧梺鎸庣⊕閻＄嫕piErrorResponse`

### 10.6 POST `/api/notifications/broadcast/success`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗亞閻?`admin`
- Query 闂佸憡鐟ラ崐褰掑汲閻斿吋鏅慨婵囶劗itle`闂侀潧妫斿姊瀍ssage`
- 闂佺懓鐡ㄩ崝鏇熸叏濞戞瑦浜ら柡鍌涘缁€鈧梺鎸庣⊕濮樸劑宕欐禒瀣闁搞儯鍔嶉幏?- 婵犮垺鍎肩划鍓ф喆閿旇姤浜ら柡鍌涘缁€鈧梺鎸庣⊕閻＄嫕piErrorResponse`

### 10.7 POST `/api/notifications/broadcast/error`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗亞閻?`admin`
- Query 闂佸憡鐟ラ崐褰掑汲閻斿吋鏅慨婵囶劗itle`闂侀潧妫斿姊瀍ssage`
- 闂佺懓鐡ㄩ崝鏇熸叏濞戞瑦浜ら柡鍌涘缁€鈧梺鎸庣⊕濮樸劑宕欐禒瀣闁搞儯鍔嶉幏?- 婵犮垺鍎肩划鍓ф喆閿旇姤浜ら柡鍌涘缁€鈧梺鎸庣⊕閻＄嫕piErrorResponse`

## 11. SSE 缂備胶濯寸槐鏇㈠箖婵犲洤绠抽柕澶堝劚缂?
### 11.1 GET `/api/notifications/manage/connections`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗亞閻?`admin`
- 闂佺懓鐡ㄩ崝鏇熸叏濞戞瑦浜ら柡鍌涘缁€鈧梺鎸庣⊕閼规儳锕㈤鍡欐／闁兼剚鍨崇粻楣冩煙閹帒鍔欏ǎ鍥э躬楠?JSON
- 婵犮垺鍎肩划鍓ф喆閿旇姤浜ら柡鍌涘缁€鈧梺鎸庣⊕閻＄嫕piErrorResponse`

### 11.2 GET `/api/notifications/manage/check/{userId}`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗幗閹峰崬霉濠婂懎鍔嬮柛?`admin`
- 闂佺懓鐡ㄩ崝鏇熸叏濞戞瑦浜ら柡鍌涘缁€鈧梺鎸庣⊕濮樸劑寮妶澶婄闁汇値鍨崇粻楣冩煙閹帒鍔ゅ┑顔芥倐楠炩偓?JSON
- 婵犮垺鍎肩划鍓ф喆閿旇姤浜ら柡鍌涘缁€鈧梺鎸庣⊕閻＄嫕piErrorResponse`

### 11.3 POST `/api/notifications/manage/disconnect/{userId}`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗幗閹峰崬霉濠婂懎鍔嬮柛?`admin`
- 闂佺懓鐡ㄩ崝鏇熸叏濞戞瑦浜ら柡鍌涘缁€鈧梺鎸庣⊕濮樸劑宕欐禒瀣闁搞儯鍔嶉幏?- 婵犮垺鍎肩划鍓ф喆閿旇姤浜ら柡鍌涘缁€鈧梺鎸庣⊕閻＄嫕piErrorResponse`

### 11.4 GET `/api/notifications/manage/stats`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗亞閻?`admin`
- 闂佺懓鐡ㄩ崝鏇熸叏濞戞瑦浜ら柡鍌涘缁€鈧梺鎸庣⊕娣囪櫣鎹㈠☉銏犵闁靛／鍛厷闁?JSON
- 婵犮垺鍎肩划鍓ф喆閿旇姤浜ら柡鍌涘缁€鈧梺鎸庣⊕閻＄嫕piErrorResponse`

## 12. 闂傚倸妫楀Λ娆撳垂濮樿埖鍋愰柤鍝ヮ暯閸嬫挻鎷呴悷鏉款槻闂?
闁哄鏅滈悷褏鍒掑澶婄闁靛鍎辩紞鎾斥槈閹惧瓨顫楁い鈺嬪缁辨帒鈻庤箛鏂库偓鐢告煕濞嗘劕顥嬫い鎾存倐閹爼宕遍幇銊ヤ壕濞达絿鏅粻銉х磽娴ｈ灏﹂柕鍡楊樀楠炲寮借閸婄數绱撴笟鍥у箹濠⒀呭Х缁晠顢涘┑鍡楀箣闂佸憡姊圭粙鎴﹀垂娴犲妫樻い鎾寸箓閳诲繘鏌ｉ～顒€濡搁柍?
### 12.1 GET `/api/integrations/status`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗亞閻?`admin`
- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃顑梟tegrationModuleStatusDTO[]`
- 婵犮垺鍎肩划鍓ф喆閿曞倹鏅慨婵囶儖piErrorResponse`

閻熸粎澧楅幐鍛婃櫠閻樺啿顕辨慨妯虹－缁犳煡鏌涢妷褍浠ф繛鍫熷灥铻ｉ柍銉ㄦ珪閸嬨儵鏌涢弽褎鍣洪悗姘煎弮閺?
- `kafka`
- `elasticsearch`
- `payment`
- `dubbo`
- `nacos-discovery`
- `nacos-config`

### 12.2 GET `/api/integrations/status/{module}`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗亞閻?`admin`
- Path 闂佸憡鐟ラ崐褰掑汲閻斿吋鏅慨婵囶劎odule`
- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃顑梟tegrationModuleStatusDTO`
- 婵犮垺鍎肩划鍓ф喆閿曞倹鏅慨婵囶儖piErrorResponse`

## 13. 闂佽　鍋撴い鏍ㄧ懅鐢盯鏌熼幁鎺戝姎鐟?
閻熸粎澧楅幐鍛婃櫠閻樿缁╂い鏍ㄧ懅鐢稒淇婇妞诲亾閾忣偄浠撮悗瑙勭摃鐏忔瑧鍒掗敍鍕仒闁靛鏅濈粔鐑芥煙閹帒鍔氱憸鐗堢⊕椤ㄣ儵濡搁敂鍙ユ闂佹寧绋戞總鏃傚緤閻愵剦娓舵俊顖涱儥閸氬洭鏌涜箛鏂库枙婵☆偅鎸虫俊?
### 13.1 GET `/api/payments/status`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗亝瀹曟煡鏌熼弶璺ㄧ闁告埊绻濋幆鍌滄嫚閼碱剛协闂佹椿娼块崝宥夊春?- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃鐡塳ApiResponse<Map>`

缂備讲鍋撻弶鐐村娴兼劙鏌?
```json
{
  "code": 200,
  "message": "Operation succeeded",
  "data": {
    "enabled": false,
    "provider": "stripe",
    "defaultCurrency": "usd",
    "implemented": false
  }
}
```

### 13.2 POST `/api/payments/intents`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗亝瀹曟煡鏌熼弶璺ㄧ闁告埊绻濋幆鍌滄嫚閼碱剛协闂佹椿娼块崝宥夊春?- 闁荤姴娲弨閬嶆儑閻楀牊濯撮柟鍨缁愭PaymentIntentRequest`
- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃鐡塳ApiResponse<MkPaymentIntentDTO>`

閻熸粎澧楅幐鍛婃櫠閻樺灚鍋橀悘鐐跺缁€瀣煥?
- 濠碘槅鍨埀顒冩珪閸嬨儵鏌涜箛鏂库枙婵☆偅鎸冲顕€骞嗛幍顔绢唹闂?`503`
- 濠碘槅鍨埀顒冩珪閸嬨儳鈧鍠掗崑鎾绘煕濮樼厧鐏熺紒鏌ョ畺瀵敻顢旈崨顓烆槻闂佺绻堥崕鍐诧耿閿涘嫧鍋?Stripe 闂佸搫鍟崕鑲╂崲閹达箑鐐?`501`

### 13.3 POST `/api/payments/intents/{paymentIntentId}/confirm`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗亝瀹曟煡鏌熼弶璺ㄧ闁告埊绻濋幆鍌滄嫚閼碱剛协闂佹椿娼块崝宥夊春?- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃鐡塳ApiResponse<MkPaymentIntentDTO>`

### 13.4 POST `/api/payments/intents/{paymentIntentId}/cancel`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗亝瀹曟煡鏌熼弶璺ㄧ闁告埊绻濋幆鍌滄嫚閼碱剛协闂佹椿娼块崝宥夊春?- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃鐡塳ApiResponse<MkPaymentIntentDTO>`

### 13.5 POST `/api/payments/checkout-sessions`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗亝瀹曟煡鏌熼弶璺ㄧ闁告埊绻濋幆鍌滄嫚閼碱剛协闂佹椿娼块崝宥夊春?- 闁荤姴娲弨閬嶆儑閻楀牊濯撮柟鍨缁愭MkCheckoutSession`
- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃鐡塳ApiResponse<MkPaymentIntentDTO>`

閻熸粎澧楅幐鍛婃櫠閻樺灚鍋橀悘鐐跺缁€瀣煥?
- 闁荤姴娲弨閬嶆儑閻楀牊濯撮柟鎹愬皺缁愭鏌涚€ｎ偆顣叉繛鎾冲娴滄瓕绠涢幘鑸垫喕濠电偛鐗嗛悘姘舵偋鎼淬埄娈?- 濠碘槅鍨埀顒冩珪閸嬨儵鏌涜箛鏂库枙婵☆偅鎸冲顕€骞嗛幍顔绢唹闂?`503`
- 濠碘槅鍨埀顒冩珪閸嬨儳鈧鍠掗崑鎾绘煕濮樼厧鐏熺紒鏌ョ畺瀵敻顢旈崨顓烆槻闂佺绻堥崕鍐诧耿閿涘嫧鍋?Stripe 闂佸搫鍟崕鑲╂崲閹达箑鐐?`501`

## 14. Kafka 闁荤姴顑呴崯鎶芥儊椤栫偛绠抽柕澶堝劚缂?
閻熸粎澧楅幐鍛婃櫠?Kafka 濠碘槅鍨埀顒冩珪閸嬨儳鈧鐡曠亸娆戝垝閿熺姴绀傜€瑰嫰鍋婂Σ鐢告煃閵夛妇鐭婄憸鏉垮€搁埥澶愬醇閻斿墎绀€ + 闂佺粯顭堥崺鏍焵椤戣法鍔嶉柣鎿勭節閹?+ 闂佸搫鐗冮崑鎾诲级閳哄倸鐏︾紒澶屽厴楠炰線顢涘☉姘绩闂佹椿浜滈鍌炲焵椤掍胶绠樻繛鍫熷灴瀵敻鍩€椤掑倷鐒婇煫鍥ㄦ处閸氣偓闂佽崵鍋涘Λ婊堝礂濮椻偓瀹曟繂鈽夊畷鍥╊槷婵炶揪绲藉Λ婵堝垝椤栨粍濯奸柕鍫濇噹瑜扮娀姊婚崒銈呭箲闁?
### 14.1 POST `/api/kafka/send`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗亞閻?`admin`
- Query 闂佸憡鐟ラ崐褰掑汲閻斿吋鏅?  - `topic`闂佹寧绋掗懝鎹愩亹閺屻儲鐒?  - `key`闂佹寧绋掗懝鎹愩亹閺屻儲鐒诲鑸电〒缁€澶婎潡濞戞瑯鐒炬い?`message`
  - `message`闂佹寧绋掗懝鍓ф崲閳ь剙鈹?- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃鐡塳ApiResponse<String>`

### 14.2 GET `/api/kafka/listen`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗亞閻?`admin`
- Query 闂佸憡鐟ラ崐褰掑汲閻斿吋鏅?  - `topic`闂佹寧绋掗懝鎹愩亹閺屻儲鐒?  - `key`闂佹寧绋掗懝鎹愩亹閺屻儲鐒诲鑸电〒缁€澶婎潡濞戞瑯鐒炬い?`message`
  - `message`闂佹寧绋掗懝鍓ф崲閳ь剙鈹?- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃鐡塳ApiResponse<String>`

闁荤姴娲ら悺銊ノｉ幋锔芥櫖?
- 闁荤姴娲㈤崕鑼暜閹绢喖鐭楅柨婵嗘噺閸婃娊鎮楀☉娆嶄沪缂侇喗鐩畷?`listen`闂佹寧绋戞總鏃傚緤閹规劑浜归柟鎯у暱椤ゅ懘鎮楅崷顓炰户妤犵偛娲鐢割敆閸愩劎鐛╅柡澶嗘櫆钃辨俊鑼焾闇夐悗锝庡幘濡叉悂寮堕悜鍡楀鐟滄澘鍊块弫宥囦沪閼测晝鎲归梺鍝勫閸ㄥ啿锕㈤垾鎰佹桨闁挎繂娲﹂悾杈ㄧ箾閹存繄澧ｉ柛銊ュ€块幆鍕箣濠靛洤鍓?API

### 14.3 GET `/api/kafka/status`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗亞閻?`admin`
- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃鐡塳ApiResponse<Map>`

闁哄鏅滈弻銊ッ洪弽顐熷亾濞戞瑯娈曟い鏂跨灱缁牊娼幍顔荤矗闂?
```json
{
  "enabled": false,
  "consumerEnabled": false,
  "consumedCount": 0,
  "implemented": true
}
```

### 14.4 GET `/api/kafka/messages`

- 闂備焦娼欓悺銊ヮ焽閸儲鏅慨姗嗗幗绗?- 闂佸搫顦崯鏉戭瀶濞差亝鏅慨姗嗗亞閻?`admin`
- Query 闂佸憡鐟ラ崐褰掑汲閻斿吋鏅?  - `limit`闂佹寧绋掗懝鎹愩亹閺屻儲鐒诲鑸电〒缁€澶婎潡濞戞瑯鐒炬い?`20`闂佹寧绋戦惌浣衡偓鍨墵瀹?`1-100`
- 闁哄鏅滈弻銊ッ洪弽顓熸櫖婵繃鐡塳ApiResponse<KafkaMessageRecord[]>`

闁荤姴娲ら悺銊ノｉ幋锔芥櫖?
- 闁哄鏅滈弻銊ッ洪弽顓熷剭闁告洦鍓氱瑧闁哄鏅滅粙鎾诲煝婵傜绀冮柛娑卞灡濡﹪鏌熼煬鎻掆偓妤€鈻撻幋锕€瀚夐柍褜鍓氬濠氬箣閻愭晫銉╂偣閹邦垼娼愮紒澶屽厴楠炰線顢涘鍛倞闂佸憡鐟辩紞鍥╂濠靛洨鈻旂€广儱妫欑瑧婵?Kafka 闂備焦褰冪粔鐢稿蓟婵犲洤绠璺猴工閸у﹪鏌涘▎蹇撶骇闁哄棛鍠栭獮?
## 15. 闁汇埄鍨遍悺鏇綖閸℃稒鍋愰柤鍝ヮ暯閸嬫挻鎷呴崨濠傤伅婵炴垶鎸婚懝鐐叏閻斿吋鍎?
HTTP 闂佺粯顭堥崺鏍焵椤戣儻鍏岄柣鏍ㄧ矒閺?
- `200`闂佹寧绋掔喊宥夊垂濮樿泛绀?- `400`闂佹寧绋掗懝鎯ь嚕椤掑嫬鏋佸ù鐓庣摠閺呪晠鎮归崶璺虹仜闁逞屽厸濞村洭顢氶鈧晥闁稿本鐟х粔鐓庘槈閹惧磭孝闁诡喖楠搁埢鏃堝即濡嘲浜惧ù锝堫潐閿涘鏌￠崼銏犳灆閻庡灚鐗犲畷鍫曞箻椤旇姤娅撻柣鐘叉祩閸ㄦ娊鎮?- `401`闂佹寧绋掔喊宥咃耿椤撱垺鍎岄悹鍥皺缁夊潡鏌?token 闂佸搫鍟版慨鐢稿疾?- `403`闂佹寧绋掗懝楣冨礄閿熺姵鍎岄悹鍥皺缁夊灝霉閿濆懏顥炴俊顐犲€濆鍫曞礃椤撗冩櫛闂傚倸鍋嗛崰娑㈩敋濮樿鲸灏庨柛鏇ㄥ墰閻?- `404`闂佹寧绋掓穱铏圭矈椤愨懇鏀﹂柟閭︿簽閻熸繈鎮楀☉娅亜锕?- `500`闂佹寧绋掔喊宥咃耿閸ヮ剙绀夐柨娑樺娴煎倻鈧鍠栭崐鎼佹偉?
婵炴垶鎸婚懝鐐叏閻斿吋鍎樺〒姘功缁?
- `200`闂佹寧绋掔喊宥夊垂濮樿泛绀?- `400`闂佹寧绋掗懝鎯ь嚕椤掑嫬鏋佸ù鐓庣摠閺呪晠鎮?- `401`闂佹寧绋掓穱娲敇閼姐倖瀚氬ù锝嗘偠娴滃ジ鎮?- `403`闂佹寧绋掔喊宥咁焽閸儲鈷旈柟閭︿簽閻熸繈鎮?- `404`闂佹寧绋掓穱铏圭矈椤愨懇鏀﹂柟閭︿簽閻熸繈鎮楀☉娅亜锕?- `409`闂佹寧绋掗懝楣冨疮鐠恒劎鐜?- `501`闂佹寧绋掗懝楣冨礄閳╁喛绱ｉ柛鏇ㄥ幗濞堝矂鏌熼幁鎺戝姎鐟滅増绋掗幏鍛村幢濡崵鏆為梺鍝勭墱娴滄粓鎮块崟顖涘仢闁规鍠楅崺鍌炴倵閸︻厼浠╅柛蹇旓耿瀹?- `503`闂佹寧绋掔喊宥堝暞闂佺鍕垫當闁告鍥ㄢ拻?- `500`闂佹寧绋掔喊宥咃耿閸ヮ剙绀夐柨娑樺娴煎倻鈧鍠栭崐鎼佹偉?
## 16. 闂佸憡鎸哥粔鍫曨敂椤掑嫬绠抽柕澶堝劚瀵磭鈧偣鍊濈紓姘额敊?
### 16.1 闂佽浜介崝蹇撶暦濮椻偓楠炴帡濡烽妷銉ユ辈婵＄偑鍊曢幖顐よ姳?
1. 闂佽皫鍡╁殭缂傚秴绉规俊瀛樻媴鐟欏嫭娈橀梺鍛婂姇鐞氼偊鍩€椤戣法顑刼ogle 闂佽皫鍡╁殭缂?2. 閻熸粎澧楅幐鍛婃櫠閻樼粯鍋ㄩ柕濠忕畱閻撴洟鎮硅鐎氼參寮搁敍鍕珰閻犲洦褰冪徊鍨槈閹惧啿顒㈡繛鎻掓健瀵?3. 闂佸憡甯掑Λ娑氭偖椤愶箑绀嗘俊銈呭閳ь剙鍟粙澶嬫償閳ュ磭鈧崵绱?CRUD
4. 闁荤姳鐒︽刊鑺ュ緞閸曨垰绀嗘俊銈呭閳ь剙鍟存俊瀛樻媴缁涘鏁归柣?CRUD闂侀潧妫旈悞锕傛儑瑜版帒绠?5. Excel 闁诲海鏁搁崢褔宕?6. SSE 闁诲骸婀遍崑鐐差渻閸岀偞鐒绘慨妯夸含閸欌偓
7. 闂傚倸妫楀Λ娆撳垂濮樿埖鍋愰柤鍝ヮ暯閸嬫挻绗熼埀顒勫Υ婢舵劕绠ｉ柡宥庡亽閸氣偓闂佽崵鍋涘Λ婵嬪Υ?8. Kafka 闁荤姴顑呴崯鎶芥儊椤栨稏浜滈柣鎰帨閸嬫挻鎷呯憴鍕婵炲濮甸敋鐎规洜澧楅幏鍛吋婢跺牃鍋?
### 16.2 閻熸粎澧楅幐鍛婃櫠閻樼粯顥嗛柍褜鍓涢幉鐗堟媴缁嬫鏋€缂備焦妫忛崹铏珶閹烘绀嗘い鎰剁稻閺嗗牓鏌熼弶璺ㄧ婵炲牊鍨块幃?
- 闁荤姳闄嶉崐娑㈡儊婢舵劕绠抽柕澶堝劚缂嶆捇鏌曢崱鏇犲妽闁轰緡鍠楃粋鎺懳熺拠鎻掝槻闂佸憡鐟辩徊濠氬焵椤戣法鏁╝fka 闂佽浜介崕杈亹濞戙垺鐒鹃柛濠勫枔缁犵兘鏌?`MkApiResponse`闂佹寧绋戦惌渚€銆呴敂钘夘嚤婵☆垰鎼敮銉╂煟?`code`
- `payments` 濠碘槅鍨埀顒冩珪閸嬨儱顫楀☉娆樼劸妞ゆ挸顭峰畷妤呭箮閼恒儻绱ㄩ梺鎸庣☉閼活垱鏅堕悩鐢靛崥妞ゆ牗姘ㄧ€瑰鏌涜箛鎾跺ⅲ妞?`/api/payments/status`
- `kafka` 濠碘槅鍨埀顒冩珪閸嬨儱顫楀☉娆樼劸妞ゆ挸顭峰畷妤呭箮閼恒儻绱ㄩ梺鎸庣☉閼活垶骞冨Δ鍛煑濞村吋鐣埀顒€顦伴幆鏃堝棘閸喖寮楅柣?`/api/kafka/status` 闂?`/api/integrations/status`
- `integrations` 闂佸搫瀚烽崹鎶筋敇閹间焦鍋犻柛鈩冾殕閸犲懘鏌熼幁鎺戝姎鐟滅増鐩弫宥囦沪閼测晝鎲归梻渚囧亜閸婃悂骞冩惔銊ョ柧妞ゆ洍鍋撻柍褜鍓氬銊╁极閵堝绠ｉ柣鎴濐潟閳ь剙顦靛Λ鍐閵忥紕鍑介梺瑙勪航閸庢挳鎯冮悢鍏煎仺?- `notifications` 闂?`type` 缂傚倷缍€閸涱垱鏆伴梺鍦焾椤︻垶鎯冮鍕婵炲棗绻掗幗鐔虹磼濡ゅ绱伴悷鏇炴鐎靛ジ鎮崨顖滎槷婵炴挻鑹鹃鍛淬€?`warning`
- SSE 闁荤姳闄嶉崹钘壩ｉ崟顒傤浄鐎广儱娲ゆ慨褍鈽夐幘宕囆ラ柛蹇旓耿閹嫮鈧稒锚婢跺秹姊婚崼銏犘㈤悽顖ｅ亰閹?`EventSource` 闂佸憡鐟﹂崹鍧楀焵椤戣法绐斿鐟扮焸瀵爼宕橀妸锝勭矒

### 16.3 閻熸粎澧楅幐鍛婃櫠閻樺磭顩风€广儱妫欏鎾绘倵閻熸媽瀚伴柛娆忕箳閳ь剙婀遍崑鐔肩嵁閸ヮ剚鍎嶉柛鏇ㄥ墮閳锋牠鎮橀悙瀛樼閼垛晠鏌?
- `payment`闂佹寧绋掔喊宥囨暜閹绢喖鐭楅柨婵嗩樈閳ь剙娲鍝ユ崉閸濆嫬娈ラ悗鐐瑰€曢幖顐﹀Χ娴犲鏅悘鐐跺亹缁嬪鏌ｉ鍡楁瀻闁?Stripe 濠电偟绻濋懗鍫曞煝閼测晙鐒婃慨姗嗗幗瀵捇鏌熼幁鎺戝姎闁?- `elasticsearch`闂佹寧绋掗懝楣冾敇瑜版帒绠ｅ瀣瘨娴煎倿鏌涘鍐伇濠殿喗鎮傞獮鈧ù锝囶焾閸ゆ帡鏌涜箛鎾虫Щ妞わ富鍓熼弫宥囦沪閻愵剙浠㈢紓?API 闁诲繐绻戠喊宥咃耿椤撱垺濯伴柦妯侯槹閸?- `dubbo`闂佹寧绋掗懝楣冩偄閳ь剛鐥娑樹壕闂佸憡鍑归崹鐗堢閻樿妞?trace filter 闂佸搫鐗嗛ˇ顔捐姳閿熺姵鏅悘鐐垫櫕閺変粙姊?RPC 婵炴垶鎸婚懝鐐叏閻斿憡浜ゆ俊顖滅帛瀵捇鎮橀悙瀛樼缂?- `nacos`闂佹寧绋掗惌顔剧礊閺冣偓缁嬪鎮滃Ο缁樻畼闂?闂備焦婢樼粔鍫曟偪閸℃鈻旀い鎾跺仧婵″洭鏌ｉ妸銉ヮ仾閻㈩垱鎸冲畷妤呭Ψ閵娧呭骄闂佷紮绲介懟顖炲礄閳╁啰鈹嶆繝闈涚墛濞堝矂鏌ㄥ☉妯垮闁汇劌顭峰鐢割敂閸曨喖鐓傞梺鐟扮摠閸旀洟鎮鹃鍕瀬缂傚牏濮烽悷褰掓煕閺冩挾绡€婵☆偅鎸抽幃?## 17. Search APIs (Elasticsearch)

These endpoints use `ApiErrorResponse` for non-2xx failures.
The module is disabled by default and must be enabled with `app.elasticsearch.enabled=true` before frontend testing.
When disabled or not ready, the backend returns `503 Service Unavailable`.

### 17.1 GET `/api/search/records`

- Auth: yes
- Role: self or `admin`
- Success response: `RecordSearchResultDTO[]`
- Failure response: `ApiErrorResponse`
- If `userId` is omitted, the backend uses the current authenticated user.
- Normal users cannot search another user's records.

Query params:
- `userId`: optional `Long`, admin can use it to search a target user
- `query`: optional `String`, full-text search over `notes`, `categoryName`, `type`
- `type`: optional `String`
- `categoryId`: optional `Long`
- `categoryName`: optional `String`
- `startDate`: optional `yyyy-MM-dd`
- `endDate`: optional `yyyy-MM-dd`
- `limit`: optional `Integer`, default `20`, range `1-100`

Example:
```http
GET /api/search/records?query=lunch&type=expense&limit=10
Authorization: Bearer <token>
```

### 17.2 POST `/api/search/records/reindex`

- Auth: yes
- Role: `admin` only
- Success response: `RecordSearchReindexResultDTO`
- Failure response: `ApiErrorResponse`
- If `userId` is omitted, the backend rebuilds the full record index.
- If `userId` is provided, the backend rebuilds only that user's record documents.

Query params:
- `userId`: optional `Long`

Example:
```http
POST /api/search/records/reindex?userId=1
Authorization: Bearer <token>
```

### 17.3 GET `/api/search/records/stats`

- Auth: yes
- Role: `admin` only
- Success response: `RecordSearchIndexStatsDTO`
- Failure response: `ApiErrorResponse`
- Used for Elasticsearch index visibility and backend-vs-index count comparison.
- If `userId` is omitted, the backend returns stats for the full index.
- If `userId` is provided, the backend returns stats for that user's subset.

Query params:
- `userId`: optional `Long`

Example:
```http
GET /api/search/records/stats?userId=1
Authorization: Bearer <token>
```

### 17.4 RecordSearchResultDTO

```json
{
  "id": 11,
  "userId": 1,
  "categoryId": 5,
  "categoryName": "Food",
  "type": "expense",
  "amount": 18.50,
  "transactionDate": "2026-03-08",
  "updatedAt": "2026-03-08T12:00:00",
  "notes": "Lunch with team",
  "score": 1.25
}
```

### 17.5 RecordSearchReindexResultDTO

```json
{
  "scope": "user",
  "userId": 1,
  "indexedCount": 42,
  "indexName": "moneykeeper-records",
  "reindexedAt": "2026-03-08T12:00:00"
}
```

### 17.6 RecordSearchIndexStatsDTO

```json
{
  "scope": "user",
  "userId": 1,
  "enabled": true,
  "ready": true,
  "indexName": "moneykeeper-records",
  "indexExists": true,
  "indexedDocumentCount": 40,
  "databaseRecordCount": 42,
  "statsCollectedAt": "2026-03-09T09:00:00"
}
```

Field notes:
- `indexedDocumentCount`: current Elasticsearch document count for the selected scope
- `databaseRecordCount`: current active MySQL record count for the selected scope
- `indexExists`: whether the Elasticsearch index currently exists
- `ready`: whether the backend has an enabled feature flag and a ready Elasticsearch client bean

### 17.7 Frontend Notes

- Search result data is synchronized after writes: record create/update/delete uses incremental sync, and category update/delete refreshes only the affected category's records.
- Search APIs are separate from the normal record list APIs; they are intended for keyword/filter search scenarios.
- `stats` is an admin-only operational endpoint and should generally be used in admin or internal tooling pages.
- Before showing a search UI in production or staging, check `/api/integrations/status/elasticsearch` with an admin account, or make sure deployment has explicitly enabled the module.
- Full or per-user rebuild is still available through `/api/search/records/reindex` for repair and backfill scenarios.