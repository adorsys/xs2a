# Versioning, Release and Support policy

In general XS2A Team follows [SemVer](http://semver.org/) for versioning.

This means our versions follow the model **A.B.C**, where:
* **A** - major version, pointing out mainline.
* **B** - minor version, points out next release in the mainline. Minimum 2 versions backward compatibility is guaranteed for _stable_ mainlines.
* **C** - hotfix version, used to deliver patches between releases in case of need. May be omitted, therefore version `4.5` would equal to `4.5.0`.

We support one stable and one development version at glance.

To keep it simple:
* We use **even** major version to mark stable support mainlines (`2.x`, `4.x`, `6.x` etc)
* We use **odd** major version to mark development mainlines (`1.x`, `3.x`, `5.x` etc)

Normally new development version is released every two weeks, however this is not a strict rule, rather our willingness.

[adorsys Team](https://adorsys.de/en/psd2) guarantees general support of this XS2A library at least till 01.03.2022.

### Backward compatibility
For stable mainlines we provide backward compatiblity of APIs and Database schema.
Although for stable versions backward compatibility is high priority and we try our best to keep it as much as possible,
we can guarantee backward compatibility only for two versions before.

I.e. if you get version `2.25`, it will keep backward compatibility with `2.24` and `2.23`,
but some changes may appear between APIs of version `2.25` with version `2.22`.

The same is valid for the database schema.

If you need extended support, please contact [adorsys Team](https://adorsys.de/en/psd2).

## Stable versions

Stable versions are recommended for production usage. Normally they have support period of time at least 6 months.
If you need additional support, please contact [adorsys Team](https://adorsys.de/en/psd2).

### Mainline 2.x
Mainline 2.x is released on 01.03.2019. The main goal is to support mandatory requirements of Berlin Group specification 1.3.
This version will be supported at least till 01.09.2019.

## Development versions

### Mainline 3.x
The goal is to implement all additional features defined in the Berlin group specification 1.3.

### Mainline 1.x (till 01.03.2019)
Initial development of XS2A library and corresponding systems.

For upcoming versions and features see our [Roadmap](roadmap.md).

For the versions available, see the [tags on this repository](https://github.com/your/project/tags)
and [Release notes for each version](releasenotes.md).

