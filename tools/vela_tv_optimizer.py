#!/usr/bin/env python3
"""Vela TV Optimizer: offline, read-only analysis of a user-supplied package snapshot.

Input JSON: {"device": {...}, "packages": [{"name": "...", "enabled": true,
"roles": ["recommendation"], "evidence": ["..."], "required_by": ["..."]}]}.
No ADB connection or package-state mutation is performed.
"""
import argparse
import json
from pathlib import Path

PROTECTED = {
    'com.android.systemui', 'com.android.packageinstaller', 'com.google.android.gms',
    'com.android.vending', 'com.android.settings', 'com.android.bluetooth',
    'com.google.android.tvlauncher', 'com.tcl.tvlauncher',
}
PROTECTED_ROLES = {
    'stock_home', 'system_ui', 'package_installer', 'settings', 'play_services',
    'play_store', 'network', 'wifi', 'bluetooth', 'remote_input', 'hdmi_cec',
    'live_tv', 'drm', 'media_playback', 'system_update', 'account_auth',
}
REVIEW_ROLES = {'vendor_promotion', 'recommendation', 'telemetry', 'analytics'}


def analyze(snapshot):
    packages = snapshot.get('packages', [])
    if not isinstance(packages, list):
        raise ValueError('packages must be a list')
    seen = set()
    findings = []
    for package in packages:
        name = package['name']
        if name in seen:
            raise ValueError('duplicate package: ' + name)
        seen.add(name)
        if not isinstance(package.get('enabled'), bool):
            raise ValueError('enabled must be recorded for ' + name)
        roles = set(package.get('roles', []))
        evidence = package.get('evidence', [])
        dependents = package.get('required_by', [])
        protected = name in PROTECTED or bool(roles & PROTECTED_ROLES)
        review = bool(roles & REVIEW_ROLES) and bool(evidence)
        category = 'Protected — Never Automatically Disable' if protected else (
            'Review Required' if review else 'Unclassified — More Evidence Required')
        # No package is recommended for disabling automatically. Dependencies are shown for human review.
        findings.append({
            'package': name, 'original_enabled': package['enabled'],
            'category': category, 'evidence': evidence, 'required_by': dependents,
            'benefit': 'May reduce optional promotions or telemetry' if review and not protected else None,
            'side_effects': 'Unknown until component and dependency review' if review else None,
            'proposed_action': 'manual_review' if review and not protected else 'none',
            'conditional_rollback': ('adb shell pm enable --user 0 ' + name) if package['enabled']
                else ('adb shell pm disable-user --user 0 ' + name),
        })
    return {
        'device': snapshot.get('device', {}), 'mode': 'analysis_only',
        'original_states_recorded': True, 'changes_executed': [],
        'safe_home_ui_changes': ['Keep Vela Home free of recommendations and advertising',
                                 'Offer reduced motion in Launcher settings'],
        'findings': findings,
        'approval_requirement': 'Exact package list and tested recovery path required before any future change',
    }


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('snapshot', type=Path, help='JSON snapshot collected with authorized read-only access')
    parser.add_argument('--output', type=Path, help='Optional JSON report path')
    args = parser.parse_args()
    report = json.dumps(analyze(json.loads(args.snapshot.read_text(encoding='utf-8'))),
                        ensure_ascii=False, indent=2) + '\n'
    if args.output:
        args.output.write_text(report, encoding='utf-8')
    else:
        print(report, end='')


if __name__ == '__main__':
    main()
