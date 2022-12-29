package dev.rosewood.rosechat.locale;

import dev.rosewood.rosegarden.locale.Locale;
import java.util.LinkedHashMap;
import java.util.Map;

public class JapaneseLocale implements Locale {

    @Override
    public String getLocaleName() {
        return "ja_JP";
    }

    @Override
    public String getTranslatorName() {
        return "Lilac";
    }

    @Override
    public Map<String, Object> getDefaultLocaleValues() {
        return new LinkedHashMap<String, Object>() {{
            this.put("#0", "プラグイン・メッセージ・プレフィックス ");
            this.put("prefix", "&7[<g:#8A2387:#E94057:#F27121>RoseChat&7] ");

            this.put("#1", "コマンド・メッセージ");
            this.put("no-permission", "&cこれをするのは許可がありません！");
            this.put("player-not-found", "&cこのプレイヤーはオンラインではありません！");
            this.put("player-only", "&cコンソールはこのコマンドを使うことができません！");
            this.put("invalid-arguments", "&c使用方法： &b%syntax%&c。");
            this.put("not-a-number", "&cこれはナンバーではありません。");
            this.put("message-blank", "&cメッセージを入力してください！");

            this.put("#2", "メイン・コマンド・メッセージ");
            this.put("base-command-help", "&eコマンドインフォメーションには&b/rc help &eを使います。");

            this.put("#3", "ヘルプ・コマンド");
            this.put("command-help-description", "&8 - &d/rc help &7- ヘルプメニューを見せます…現在地。");
            this.put("command-help-title", "&e使用可能コマンド：");
            this.put("command-help-usage", "&e/rc help");

            this.put("#4", "リロード・コマンド");
            this.put("command-reload-description", "&8 - &d/rc reload &7- プラグインをリロードします。");
            this.put("command-reload-usage", "&e/rc reload");
            this.put("command-reload-reloaded", "&eプラグインのデータとコンフィグファイルとロケールファイルはリロードしました。");

            this.put("#5", "モデレーション・コマンド");
            this.put("blocked-caps", "&c大文字をすぎるのは入りますからあなたのメッセージをセンドすることができませんでした！");
            this.put("blocked-spam", "&cスパムしないでください！");
            this.put("blocked-language", "&c醜語を使いました (;_;)");
            this.put("blocked-url", "&cURLかIPをセンドしないでください！");

            this.put("#6", "メッセージ・コマンド");
            this.put("command-message-description", "&8 - &d/msg &7- プレイヤーにメッセージします。");
            this.put("command-message-usage", "&e/msg <プレイヤー> <メッセージ>");
            this.put("command-message-enter-message", "&cメッセージを入力してください！");

            this.put("#7", "答えコマンド");
            this.put("command-reply-description", "&8 - &d/reply &7- プレイヤーのメッセージに答えます。");
            this.put("command-reply-usage", "&e/reply");
            this.put("command-reply-enter-message", "&cメッセージを入力してください！");
            this.put("command-reply-no-one", "&cここでは誰もいません…");

            this.put("#8", "ソーシャルスパイ・コマンド");
            this.put("command-socialspy-description", "&8 - &d/socialspy &7- プライベートメッセージを見せるのをトグルします。");
            this.put("command-socialspy-usage", "&e/socialspy <タイプ>");
            this.put("command-socialspy-enabled", "&b%type%&eスパイを&aエネーブルしました&e。");
            this.put("command-socialspy-disabled", "&b%type%&eスパイを&cディセーブルしました&e。");
            this.put("command-socialspy-message", "メッセージ");
            this.put("command-socialspy-channel", "チャンネル");
            this.put("command-socialspy-group", "グループ");
            this.put("command-socialspy-all", "メッセージとチャンネルとグループ");

            this.put("#9", "トグルメッセージ・コマンド");
            this.put("command-togglemessage-description", "&8 - &d/togglemessage &7- メッセージを受けるのをトグルします。");
            this.put("command-togglemessage-usage", "&e/togglemessage");
            this.put("command-togglemessage-on", "&eメッセージを受けるのを&aエネーブルしました&e。");
            this.put("command-togglemessage-off", "&eメッセージを受けるのを&cディセーブルしました&e。");
            this.put("command-togglemessage-cannot-message", "&cこのプレイヤーにメッセージすることができません！");

            this.put("#10", "トグルサウンド ・コマンド");
            this.put("command-togglesound-description", "&8 - &d/togglesound &7- メッセージ音とタッグ音をトグルします。");
            this.put("command-togglesound-usage", "&e/togglesound <messages/tags/all>");
            this.put("command-togglesound-on", "&b%type%&e音を&aエネーブルしました&e。");
            this.put("command-togglesound-off", "&b%type%&e音を&cディセーブルしました&e。");
            this.put("command-togglesound-messages", "メッセージ");
            this.put("command-togglesound-tags", "タッグ");
            this.put("command-togglesound-all", "オール");

            this.put("#11", "トグルえもじ・コマンド");
            this.put("command-toggleemoji-description", "&8 - &d/toggleemoji &7- えもじのフォーマットをトグルします。");
            this.put("command-toggleemoji-usage", "&e/toggleemoji");
            this.put("command-toggleemoji-on", "&eえもじのフォーマットを&aエネーブルしました&e。");
            this.put("command-toggleemoji-off", "&eえもじのフォーマットを&cディセーブルしました&e。");

            this.put("#12", "チャンネル・コマンド");
            this.put("command-channel-description", "&8 - &d/channel &7- チャットチャンネルにメッセージをセンドします。");
            this.put("command-channel-usage", "&e/channel <チャンネル> [メッセージ]");
            this.put("command-channel-not-found", "&cこのチャンネルがありません。");
            this.put("command-channel-joined", "&e今、&b%id%&eチャンネルを使っています。");
            this.put("command-channel-custom-usage", "&e/%channel% &e<メッセージ>");
            this.put("command-channel-not-joinable", "&cこのチャンネルを参加することができません。");
            this.put("command-channel-cannot-message", "&cこのチャンネルにメッセージをセンドすることができません！");

            this.put("#13", "チャット・コマンド");
            this.put("command-chat-description", "&8 - &d/chat &7- アドミンのヘルプメニューを見せます。");
            this.put("command-chat-usage", "&e/chat help");

            this.put("#14", "チャットヘルプ・コマンド");
            this.put("command-chat-help-description", "&8 - &c/chat help &7- アドミンのヘルプメニューを見せます…現在地。");
            this.put("command-chat-help-usage", "&8 - &e/chat help");
            this.put("command-chat-help-title", "&e使用可能コマンド：");

            this.put("#15", "チャットクリア・コマンド");
            this.put("command-chat-clear-description", "&8 - &c/chat clear &7- チャットをクリアします。");
            this.put("command-chat-clear-usage", "&e/chat clear [チャンネル]");
            this.put("command-chat-clear-cleared", "&b%channel%&eチャンネルをクリアしました。");

            this.put("#16", "チャットムーブ・コマンド");
            this.put("command-chat-move-description", "&8 - &c/chat move &7- プレイヤーをチャンネルに乗り換えます。");
            this.put("command-chat-move-usage", "&e/chat move <プレイヤー> <チャンネル>");
            this.put("command-chat-move-success", "&b%player%&eを&b%channel%&eに乗り換えました。");
            this.put("command-chat-move-moved", "&b%channel%&eに乗り換えました。");

            this.put("#17", "チャットミュート・コマンド");
            this.put("command-chat-mute-description", "&8 - &c/chat mute &7- チャットチャンネルをミュートします。");
            this.put("command-chat-mute-usage", "&e/chat mute <チャンネル>");
            this.put("command-chat-mute-muted", "&b%channel%&eチャンネルをミュートしました。");
            this.put("command-chat-mute-unmuted", "&b%channel%&eチャンネルはミュートを解除しました。");
            this.put("channel-muted", "&cこのチャンネルはミュートだからこのチャンネルにメッセージをセンドすることができません。");

            this.put("#18", "チャットSudo・コマンド");
            this.put("command-chat-sudo-description", "&8 - &c/chat sudo &7- あなたは他のプレイヤーからメッセージをセンドします。");
            this.put("command-chat-sudo-usage", "&e/chat sudo <プレイヤー> <チャンネル> <メッセージ>");

            this.put("#19", "チャット・インフォ・コマンド");
            this.put("command-chat-info-description", "&8 - &c/chat info &7- チャットチャンネルのインフォメーションを見せます。");
            this.put("command-chat-info-usage", "&e/chat info <チャンネル>");
            this.put("command-chat-info-title", "&7[&c%id% インフォ&7]");
            this.put("command-chat-info-format", "&eデフォルト： &b%default% &7| &eミュート： &b%muted% &7| &eコマンド： &c%command%\n" +
                    "&eワールド： &a%world% &7| &e参加できる： &b%joinable% &7| &eディスコード： &b%discord%\n" +
                    "&eプレイヤー： &b%players% &7| &eサーバー： &b%servers%");
            this.put("command-chat-info-true", "真");
            this.put("command-chat-info-false", "偽");
            this.put("command-chat-info-none", "無");

            this.put("#20", "グループチャット・コマンド");
            this.put("command-gc-description", "&8 - &d/gc &7- グループチャットのヘルプメニューを見せます。");
            this.put("command-gc-usage", "&e/gc help");
            this.put("no-gc", "&cグループチャットを持っていません！");
            this.put("gc-invalid", "&cあなたはこのグループチャットにありません！");
            this.put("gc-does-not-exist", "&cこのグループチャットはありません！");
            this.put("gc-limit", "&cグループチャットをもっと参加することができません！");

            this.put("#21", "グループチャット・ヘルプ・コマンド");
            this.put("command-gc-help-description", "&8 - &b/gc help &7- グループチャットのヘルプメニューを見せます…現在地。");
            this.put("command-gc-help-usage", "&e/gc help");

            this.put("#22", "グループチャット・クリエイト・コマンド");
            this.put("command-gc-create-description", "&8 - &b/gc create &7- 新しいグループチャットを作ります。");
            this.put("command-gc-create-usage", "&e/gc create <id> <名前>");
            this.put("command-gc-create-success", "&e新しいグループチャットを作りました、名前は&b%name%&eです。 プレイヤーを招待に&b/gc invite&eを使います。");
            this.put("command-gc-create-fail", "&cグループチャットをもう持っています！");
            this.put("command-gc-already-exists", "&cこの名前のグループ チャットはもうあります！");

            this.put("#23", "グループチャット・インバイト・コマンド");
            this.put("command-gc-invite-description", "&8 - &b/gc invite &7- プレイヤーをグループチャットに招待します。");
            this.put("command-gc-invite-usage", "&e/gc invite <プレイヤー>");
            this.put("command-gc-invite-full", "&bあなたのグループチャットは１２８人のメンバーがもういます！");
            this.put("command-gc-invite-success", "&b%player%&eを&b%name%&eグループチャットに招待しました。");
            this.put("command-gc-invite-invited", "&b%player%&eは&b%name%&eグループチャットに招待しました。");
            this.put("command-gc-invite-member", "&cこのプレイヤーはあなたのグループチャットにもういます！");

            this.put("#24", "グループチャット・キック・コマンド");
            this.put("command-gc-kick-description", "&8 - &b/gc kick &7- プレイヤーをグループチャットから取り除きます。");
            this.put("command-gc-kick-usage", "&e/gc kick <プレイヤー>");
            this.put("command-gc-kick-success", "&b%player%&eを&b%name%&eグループチャットから取り除きました。");
            this.put("command-gc-kick-kicked", "&eあなたを&b%name%&eグループチャットから取り除きました。");
            this.put("command-gc-kick-invalid-player", "&cこのプレイヤーがあなたのグループチャットにいません！");
            this.put("command-gc-kick-self", "&c自分を取り除くことができません！");

            this.put("#25", "グループチャット・アクセプト・コマンド");
            this.put("command-gc-accept-description", "&8 - &b/gc accept &7- グループチャットの招待を応じます。");
            this.put("command-gc-accept-usage", "&e/gc accept [プレイヤー]");
            this.put("command-gc-accept-success", "&eあなたは&b%name%&eグループチャットを参加しました。");
            this.put("command-gc-accept-accepted", "&b%player%&eは&b%name%&eグループチャットを参加しました。");
            this.put("command-gc-accept-no-invites", "&cグループチャットの招待がありません (;_;)");
            this.put("command-gc-accept-not-invited", "&cあなたをこのグループチャットは招待しませんでした。");
            this.put("command-gc-accept-hover", "&a招待を応するにクリックします");
            this.put("command-gc-accept-accept", "&a&l応じます");

            this.put("#26", "グループチャット・デナイ・コマンド");
            this.put("command-gc-deny-description", "&8 - &b/gc deny &7- グループチャット招待を却下します。");
            this.put("command-gc-deny-usage", "&e/gc deny [プレイヤー]");
            this.put("command-gc-deny-success", "&b%name%&eの招待を却下しました。");
            this.put("command-gc-deny-denied", "&b%player%&eはあなたの招待を却下しました。");
            this.put("command-gc-deny-hover", "&c招待を却下するにクリックします");
            this.put("command-gc-deny-deny", "&c&l却下します");

            this.put("#27", "グループチャット・リーブ・コマンド");
            this.put("command-gc-leave-description", "&8 - &b/gc leave &7- グループチャットから離れます。");
            this.put("command-gc-leave-usage", "&e/gc leave <グループチャット>");
            this.put("command-gc-leave-success", "&eあなたは&b%name%&eグループチャットから離れました。");
            this.put("command-gc-leave-left", "&b%player%&eは&b%name%&eグループチャットから離れました。");
            this.put("command-gc-leave-own", "&cあなたのグループチャットから離れることができません！ グループチャットを解散するに&b/gc disband&cを使います。");

            this.put("#28", "グループチャット・ディスバンド・コマンド");
            this.put("command-gc-disband-description", "&8 - &b/gc disband &7- グループチャットを解散します。");
            this.put("command-gc-disband-usage", "&e/gc disband [グループチャット]");
            this.put("command-gc-disband-success", "&b%name%&eグループチャットを解散しました…");
            this.put("command-gc-disband-admin", "&eあなたは&b%name%&eグループチャットを解散しました。");

            this.put("#29", "グループチャット・メンバー・コマンド");
            this.put("command-gc-members-description", "&8 - &b/gc members &7- グループチャットのメンバーを見せます。");
            this.put("command-gc-members-usage", "&e/gc members <グループチャット>");
            this.put("command-gc-members-title", "&8[&b%name% &bメンバーズ&8]");
            this.put("command-gc-members-owner", "&8- &e:star: &b%player%");
            this.put("command-gc-members-member", "&8- &b%player%");

            this.put("#30", "グループチャット・リネーム・コマンド");
            this.put("command-gc-rename-description", "&8 - &b/gc rename &7- グループチャットをリネームします。");
            this.put("command-gc-rename-usage", "&e/gc rename <グループチャット> <新しい名前>");
            this.put("command-gc-rename-success", "&eグループチャットの名前は今&b%name%&e。");

            this.put("#31", "グループチャット・インフォ・コマンド");
            this.put("command-gc-info-description", "&8 - &b/gc info &7- グループチャットのインフォメーションを見せます。");
            this.put("command-gc-info-usage", "&e/gc info <グループチャット>");
            this.put("command-gc-info-title", "&7[&b%group% &bインフォ&7]");
            this.put("command-gc-info-format", "&eID: &b%id% &7| &eオーナー: &b%owner% &7| &eメンバー: &b%members%");

            this.put("#32", "グループチャット・メッセージ・コマンド");
            this.put("command-gcm-description", "&8 - &d/gcm &7- グループチャットにメッセージをセンドします。");
            this.put("command-gcm-usage", "&e/gcm <グループチャット> <メッセージ>");
            this.put("command-gcm-enter-message", "&cメッセージを入力してください！");

            this.put("#33", " チャット・カラー ・コマンド");
            this.put("command-color-description", "&8 - &d/color &7- あなたのデフォルトのチャット色を変更します。");
            this.put("command-color-usage", "&e/color <装飾コード>");
            this.put("command-color-success", "&e新しいチャット色は&f%color%&e&r。");
            this.put("command-color-invalid", "&eこれは有効な装飾コードではありません。");
            this.put("command-color-removed", "&eあなたのチャットカラーは外しました。");
            this.put("command-color-gradient", "グラディエント");
            this.put("command-color-rainbow", "レインボー");

            this.put("#34", "ミュート・コマンド");
            this.put("command-mute-description", "&8 - &d/mute &7- プレイヤーをミュートします。");
            this.put("command-mute-usage", "&e/mute <プレイヤー> [時間] [days/months/years]");
            this.put("command-mute-success", "&b%player%&eを&b%time%&b%scale%&eにミュートしました。");
            this.put("command-mute-indefinite", "&b%player%&eを無限の間にミュートしました。");
            this.put("command-mute-muted", "&eあなたをミュートしました。");
            this.put("command-mute-cannot-be-muted", "&cこのプレイヤーをミュートすることができません！");
            this.put("command-mute-cannot-send", "&cあなたをミュートしましたからメッセージをセンドすることができません！");
            this.put("command-mute-unmuted", "&eミュートを解除しました。");
            this.put("command-mute-second", "秒間");
            this.put("command-mute-minute", "分間");
            this.put("command-mute-hour", "時間");
            this.put("command-mute-month", "か月間");
            this.put("command-mute-year", "年間");
            this.put("command-mute-seconds", "秒間");
            this.put("command-mute-minutes", "分間");
            this.put("command-mute-hours", "時間");
            this.put("command-mute-months", "か月間");
            this.put("command-mute-years", "年間");

            this.put("#35", "アンミュート・コマンド");
            this.put("command-unmute-description", "&8 - &d/unmute &7- プレイヤーのミュートを解除しました。");
            this.put("command-unmute-usage", "&e/unmute <player>");
            this.put("command-unmute-success", "&eミュートを解除しました。");

            this.put("#36", "ニックネーム・コマンド");
            this.put("command-nickname-description", "&8 - &d/nickname &7- ニックネームを変更します。");
            this.put("command-nickname-usage", "&e/nickname <ニックネーム/プレイヤー> [ニックネーム/off]");
            this.put("command-nickname-success", "&e新しいニックネームは&f%name%&eです。");
            this.put("command-nickname-other", "&b%player%&eの新しいニックネームは&f%name%&eです。");
            this.put("command-nickname-too-short", "&cこのニックネームは短すぎます！");
            this.put("command-nickname-too-long", "&cこのニックネームは長すぎます！");
            this.put("command-nickname-not-allowed", "&cこのニックネームを使うことができません！");

            this.put("#37", "イグノア・コマンド");
            this.put("command-ignore-description", "&8 - &d/ignore &7- 他のプレイヤーのメッセージを見るのをやめます。");
            this.put("command-ignore-usage", "&e/ignore <プレイヤー>");
            this.put("command-ignore-ignored", "&b%player%&eを無視しています。");
            this.put("command-ignore-unignored", "&b%player%&eを無視していません。");

            this.put("#38", "Debug Command");
            this.put("command-debug-description", "&8 - &d/rc debug &7- デバッグモードをトグルします。");
            this.put("command-debug-on", "&eデバッグモードは今&aオン&eです。 デバッグインフォをセーブにまた&b/rc debugを使います。");
            this.put("command-debug-off", "&eデバッグモードは&cオフ&eです。 &bplugins/RoseChat/debug&eに新しいファイルをセーブしました。");
        }};
    }

}
