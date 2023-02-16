class StrictEq:
    @staticmethod
    def _compare(val_1, val_2):
        if val_1 == val_2:
            return 0
        return 1

    @staticmethod
    def difference(val_1, val_2):
        return StrictEq._compare(val_1, val_2)

    @staticmethod
    def similarity(val_1, val_2):
        return 1 - StrictEq._compare(val_1, val_2)


class FeatureCompareFunction:
    def __init__(self):
        self.func = {'eq': StrictEq(), 'allow_trait': []}

    def set_feature_func(self, trait, func_name='eq'):
        trait['cmp_crit'] = self.func[func_name]
